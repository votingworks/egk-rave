package org.cryptobiotic.verificabitur.workflow

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.unwrap
import electionguard.ballot.*
import electionguard.cli.RunBatchEncryption.Companion.batchEncryption
import electionguard.cli.RunCreateElectionConfig
import electionguard.core.*
import electionguard.decrypt.DecryptingTrusteeIF
import electionguard.keyceremony.KeyCeremonyTrustee
import electionguard.keyceremony.keyCeremonyExchange
import electionguard.publish.*
import org.cryptobiotic.pep.RunMixnetBlindTrustPep
import org.cryptobiotic.pep.makeGuardian
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Run workflow with varying number of guardians, on the same ballots.
 */
class TestPepWorkflow {
    val group = productionGroup()

    private val inputDir = "src/test/data/working"
    private val manifestJson = "$inputDir/eg/manifest.json"
    private val inputBallotDir = "$inputDir/eg/inputBallots"
    val name1 = "runWorkflowOneGuardian"
    val name2 = "runWorkflowThreeGuardian"
    val name3 = "runWorkflow5of6Guardian"
    val name4 = "runWorkflow8of10Guardian"

    @Test
    fun runPepWorkflows() {
        println("productionGroup (Default) = $group class = ${group.javaClass.name}")
        //runPepWorkflow(name1, 1, 1, listOf(1), 1)
        runPepWorkflow(name1, 1, 1, listOf(1), 25)

        //runPepWorkflow(name2, 3, 3, listOf(1,2,3), 1)
        runPepWorkflow(name2, 3, 3, listOf(1,2,3), 25)

        //runPepWorkflow(name3, 6, 5, listOf(1,2,4,5,6), 1)
        runPepWorkflow(name3, 6, 5, listOf(1,2,4,5,6), 25)

        //runPepWorkflow(name4, 10, 8, listOf(1,2,4,5,6,7,8,9), 1)
        runPepWorkflow(name4, 10, 8, listOf(1,2,4,5,6,7,8,9), 25)
    }

    fun runPepWorkflow(name : String, nguardians: Int, quorum: Int, present: List<Int>, nthreads: Int) {
        println("======================================================================================")
        val workingDir =  "testOut/workflowPep/$name"
        val trusteeDir =  "$inputDir/eg/trustees"
        val invalidDir =  "${workingDir}/invalid"
        val mixedBallots = "$inputDir/bb/vf/mix2/ShuffledCiphertexts.bt"

        // delete current workingDir
        makePublisher(workingDir, true)

        RunCreateElectionConfig.main(
            arrayOf(
                "-manifest", manifestJson,
                "-nguardians", nguardians.toString(),
                "-quorum", quorum.toString(),
                "-out", workingDir,
                "-device", "device11",
                "-createdBy", name1,
            )
        )

        // key ceremony
        val (_, init) = runFakeKeyCeremony(group, workingDir, workingDir, trusteeDir, nguardians, quorum, false)

        // encrypt
        group.getAndClearOpCounts()
        batchEncryption(group, inputDir = workingDir, ballotDir = inputBallotDir, "device11", outputDir = workingDir, null, invalidDir, nthreads, name1)
        //println("----------- after encrypt ${group.showAndClearCountPowP()}")

        // encrypt again, simulating the CAKE workflow of scanning the paper ballots
        val scannedBallotDir = "$workingDir/scan"
        batchEncryption(group, inputDir = workingDir, ballotDir = inputBallotDir, "scanPaper", outputDir = scannedBallotDir, null, invalidDir, nthreads, name1)
        //println("----------- after encrypt ${group.showAndClearCountPowP()}")

        val decryptingTrustees = readDecryptingTrustees(group, trusteeDir, init, present, true)

        group.getAndClearOpCounts()

        //  fun batchMixnetBlindTrustPep(
        //            group: GroupContext,
        //            inputDir: String,
        //            encryptedBallotsDir: String,
        //            trusteeDir: String,
        //            mixedBallots: String,
        //            isJson: Boolean,
        //            outputDir: String,
        //            nthreads: Int,
        //        )
        println("runTrustedPep n = $quorum / $nguardians")
        RunMixnetBlindTrustPep.batchMixnetBlindTrustPep(
            group,
            "$inputDir/eg",
            "$inputDir/encryptedBallots",
            trusteeDir,
            mixedBallots,
            false,
            workingDir,
            nthreads
        )

        val nballots = 33
        val nencyptions = 100
        val nb = 3
        val expect = (8 + 8 * nguardians + 8 * nb) * nencyptions  * nballots // counting the verifier
        println("----------- after compareBallotPepEquivilence ${group.getAndClearOpCounts()}, expect=$expect")
    }
}

fun readDecryptingTrustees(
    group: GroupContext,
    trusteeDir: String,
    init: ElectionInitialized,
    present: List<Int>,
    isJson: Boolean,
): List<DecryptingTrusteeIF> {
    val consumer = makeTrusteeSource(trusteeDir, group, isJson)
    return init.guardians.filter { present.contains(it.xCoordinate)}.map { consumer.readTrustee(trusteeDir, it.guardianId).unwrap() }
}

fun runFakeKeyCeremony(
    group: GroupContext,
    configDir: String,
    outputDir: String,
    trusteeDir: String,
    nguardians: Int,
    quorum: Int,
    chained: Boolean,
): Pair<Manifest, ElectionInitialized> {
    val electionRecord = readElectionRecord(group, configDir)
    val config: ElectionConfig = electionRecord.config().copy(chainConfirmationCodes = chained)

    val trustees: List<KeyCeremonyTrustee> = List(nguardians) {
        val seq = it + 1
        KeyCeremonyTrustee(group, "guardian$seq", seq, nguardians, quorum)
    }.sortedBy { it.xCoordinate }

    // exchange PublicKeys
    val exchangeResult = keyCeremonyExchange(trustees)
    if (exchangeResult is Err) {
        println("keyCeremonyExchange error = ${exchangeResult}")
    }

    // check they are complete
    trustees.forEach {
        assertTrue( it.isComplete() )
        assertEquals(quorum, it.coefficientCommitments().size)
    }

    val commitments: MutableList<ElementModP> = mutableListOf()
    trustees.forEach {
        commitments.addAll(it.coefficientCommitments())
        // it.coefficientCommitments().forEach { println("   ${it.toStringShort()}") }
    }
    assertEquals(quorum * nguardians, commitments.size)

    val jointPublicKey: ElementModP =
        trustees.map { it.guardianPublicKey() }.reduce { a, b -> a * b }

    // create a new config so the quorum, nguardians can change
    val newConfig = makeElectionConfig(
        protocolVersion,
        config.constants,
        nguardians,
        quorum,
        electionRecord.manifestBytes(),
        chained,
        config.configBaux0,
        mapOf(Pair("Created by", "runFakeKeyCeremony")),
    )
    // println("newConfig.electionBaseHash ${newConfig.electionBaseHash}")

    // He = H(HB ; 0x12, K) ; spec 2.0.0 p.25, eq 23.
    val He = electionExtendedHash(newConfig.electionBaseHash, jointPublicKey)

    val guardians: List<Guardian> = trustees.map { makeGuardian(it) }
    val init = ElectionInitialized(
        newConfig,
        jointPublicKey,
        He,
        guardians,
    )
    val publisher = makePublisher(outputDir, false, electionRecord.isJson())
    publisher.writeElectionInitialized(init)

    val trusteePublisher = makePublisher(trusteeDir, false, electionRecord.isJson())
    trustees.forEach { trusteePublisher.writeTrustee(trusteeDir, it) }

    // val decryptingTrustees: List<DecryptingTrusteeDoerre> = trustees.map { makeDoerreTrustee(it) }
    // testDoerreDecrypt(group, ElGamalPublicKey(jointPublicKey), decryptingTrustees, decryptingTrustees.map {it.xCoordinate})

    return Pair(electionRecord.manifest(), init)
}