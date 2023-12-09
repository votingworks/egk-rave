package org.cryptobiotic.verificabitur.vmn

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import electionguard.ballot.EncryptedBallot
import electionguard.core.*
import electionguard.publish.makeConsumer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.cryptobiotic.pep.MixnetPepBlindTrust
import org.cryptobiotic.pep.CiphertextDecryptor
import org.cryptobiotic.pep.PepTrustee
import org.cryptobiotic.verificabitur.bytetree.MixnetBallot
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MixnetBallotJsonReaderTest {
    val workingDir = "src/test/data/working/"
    val bbDir = "src/test/data/working/bb/vf"

    val jsonFile = "src/test/data/mixnetInput/inputCiphertexts.json"

    var fileSystem = FileSystems.getDefault()
    var fileSystemProvider = fileSystem.provider()
    val group = productionGroup()
    val jsonReader = Json { explicitNulls = false; ignoreUnknownKeys = true }


    @Test
    fun testMixnetInput() {
        val result = readMixnetBallotJson(jsonFile)
        assertTrue(result is Ok)
        val mixnetInput: MixnetBallotJson = result.unwrap() // SO?

        val decryptor = CiphertextDecryptor(
            group,
            "$workingDir/eg",
            "$workingDir/eg/trustees",
        )

        val mixnetBallots = mutableListOf<MixnetBallot>()
        val consumer = makeConsumer(group, "$workingDir/eg")
        consumer.iterateAllCastBallots().forEach { encryptedBallot ->
            val ciphertexts = mutableListOf<ElGamalCiphertext>()
            ciphertexts.add(encryptedBallot.encryptedSn!!) // always the first one
            encryptedBallot.contests.forEach { contest ->
                contest.selections.forEach { selection ->
                    ciphertexts.add(selection.encryptedVote)
                }
            }
            mixnetBallots.add(MixnetBallot(ciphertexts))
        }

        mixnetBallots.forEachIndexed { idx, it ->
            it.ciphertexts.forEach { ciphertext ->
                val vote = decryptor.decrypt(ciphertext)
                // print("$vote,")
                assertNotNull(vote)
            }
            println("\nballot ${idx + 1} OK")
        }
    }

    @Test
    fun testEncryptedBallotMatch() {
        val result = readMixnetBallotJson(jsonFile)
        assertTrue(result is Ok)
        val mixnetInput: MixnetBallotJson = result.unwrap() // SO?

        val decryptor = CiphertextDecryptor(
            group,
            "$workingDir/eg",
            "$workingDir/eg/trustees",
        )

        val encryptedBallots = mutableMapOf<Int, EncryptedBallot>() // key is the SN (for now)
        val consumer = makeConsumer(group, "$workingDir/eg")
        consumer.iterateAllCastBallots().forEach { encryptedBallot ->
            val sn = decryptor.decryptPep(encryptedBallot.encryptedSn!!)!!
            encryptedBallots[sn.hashCode()] = encryptedBallot
        }
        val blindingTrustees = mutableListOf<PepTrustee>()
        repeat(3) {
            blindingTrustees.add(PepTrustee(it, group))
        }

        val mixnetPep = MixnetPepBlindTrust(
            group,
            decryptor.init.extendedBaseHash,
            decryptor.init.jointPublicKey(),
            blindingTrustees,
            decryptor
        )

        val mixnetBallots = readMixnetJsonBallots(group, jsonFile)
        mixnetBallots.forEachIndexed { idx, it ->
            val first = decryptor.decryptPep(it.ciphertexts[0])!!
            val match = encryptedBallots[first.hashCode()]
            if (match == null) {
                println("Match ballot ${idx + 1} NOT FOUND")
            } else {
                println("Match ballot ${idx + 1} FOUND")
                val result = mixnetPep.testEquivalent(match, MixnetBallot(it.ciphertexts.subList(1, it.ciphertexts.size)))
                if (result is Err) println(result)
                assertTrue(result is Ok)
            }
        }
    }

    private fun readMixnetBallotJson(filename: String): Result<MixnetBallotJson, String> =
        try {
            val path = Path.of(filename)
            val mixnetBallotJson : MixnetBallotJson
            fileSystemProvider.newInputStream(path).use { inp ->
                val lists = jsonReader.decodeFromStream<List<List<List<String>>>>(inp)
                mixnetBallotJson = MixnetBallotJson(lists)
            }
            Ok(mixnetBallotJson)
        } catch (e: Exception) {
            e.printStackTrace()
            Err(e.message ?: "readMixnetInput on $filename error")
        }
}