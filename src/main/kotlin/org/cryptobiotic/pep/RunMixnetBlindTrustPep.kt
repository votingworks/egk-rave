package org.cryptobiotic.pep

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.unwrap

import electionguard.ballot.EncryptedBallot
import electionguard.core.*
import electionguard.publish.*
import electionguard.util.sigfig
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import io.github.oshai.kotlinlogging.KotlinLogging

import org.cryptobiotic.verificabitur.bytetree.MixnetBallot
import org.cryptobiotic.verificabitur.bytetree.readMixnetBallotFromFile
import org.cryptobiotic.verificabitur.vmn.readMixnetJsonBallots

/**
 * Compare encrypted ballots with local trustees CLI. Multithreaded: each ballot gets its own coroutine.
 *
 * Read election record from inputDir, which is assumed to have an electionInitialized file in the inputDir directory,
 * and encrypted ballots in inputDir/encrypted_ballots. This is the "standard election record layout".
 *
 * All encrypted ballots in subdirectories of inputDir/encrypted_ballots are read, and the corresponding ballot
 * (matched by ballot_id) is looked for in the scannedBallotDir. If not found, the ballot is skipped.
 * The subdirectories correspond to the "device".
 *
 * This has access to all the trustees for decrypting and blinding. So it is used when the guardians trust each other.
 * The decrypting trustees could be isolated into separate webapps, although this class does not yet have that option.
 */
class RunMixnetBlindTrustPep {

    companion object {
        private val logger = KotlinLogging.logger("RunMixnetBlindTrustPep")

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser("RunMixnetBlindTrustPep")
            val inputDir by parser.option(
                ArgType.String,
                shortName = "in",
                description = "Top directory of the input election record"
            ).required()
            val encryptedBallotsDir by parser.option(
                ArgType.String,
                shortName = "eballots",
                description = "Directory containing encrypted ballots to compare (PB)"
            ).required()
            val mixedBallots by parser.option(
                ArgType.String,
                shortName = "mixballots",
                description = "mixnet ballot output, ByteTree serialization"
            ).required()
            val mixedBallotsJson by parser.option(
                ArgType.Boolean,
                shortName = "isJson",
                description = "mixnet ballot are in Json"
            ).default(false)
            val trusteeDir by parser.option(
                ArgType.String,
                shortName = "trustees",
                description = "Directory to read private trustees"
            ).required()
            val outputDir by parser.option(
                ArgType.String,
                shortName = "out",
                description = "Directory to write output election record"
            ).required()
            val missing by parser.option(
                ArgType.String,
                shortName = "missing",
                description = "missing guardians' xcoord, comma separated, eg '2,4'"
            )
            val nthreads by parser.option(
                ArgType.Int,
                shortName = "nthreads",
                description = "Number of parallel threads to use"
            ).default(11)

            parser.parse(args)
            println(
                "RunMixnetBlindTrustPep starting\n" +
                        "   -in $inputDir\n" +
                        "   -eballots $encryptedBallotsDir\n" +
                        "   --mixedBallots $mixedBallots iJson=$mixedBallotsJson\n" +
                        "   -trustees $trusteeDir\n" +
                        "   -out $outputDir\n" +
                        "   -nthreads $nthreads"
            )
            // TODO use missing
            if (missing != null) {
                println("   -missing $missing")
            }

            val group = productionGroup()
            batchMixnetBlindTrustPep(group, inputDir, encryptedBallotsDir, trusteeDir, mixedBallots, mixedBallotsJson, outputDir, nthreads)
        }

        private fun batchMixnetBlindTrustPep(
            group: GroupContext,
            inputDir: String,
            encryptedBallotsDir: String,
            trusteeDir: String,
            mixedBallots: String,
            isJson: Boolean,
            outputDir: String,
            nthreads: Int,
        ) {
            println(" MixnetBlindTrustPep compare ballots in '${inputDir}' to ballots in '$mixedBallots'")
            val starting = getSystemTimeInMillis() // wall clock

            val decryptor = CiphertextDecryptor(
                group,
                inputDir,
                trusteeDir,
            )

            val encryptedBallots = mutableMapOf<Int, EncryptedBallot>() // key is the SN (for now)
            val consumer = makeConsumer(group, encryptedBallotsDir, true)
            consumer.iterateEncryptedBallotsFromDir(encryptedBallotsDir, null).forEach { encryptedBallot ->
                val sn = decryptor.decryptPep(encryptedBallot.encryptedSn!!)
                encryptedBallots[sn.hashCode()] = encryptedBallot
            }

            // TODO make this number settable
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
            val mixnetBallots = if (isJson) readMixnetJsonBallots(group, mixedBallots)
                                else readMixnetBallotFromFile(group, mixedBallots)

            val sink = PepIO(outputDir, group, false).pepBallotSink()

            try {
                runBlocking {
                    val outputChannel = Channel<BallotPep>()
                    val pepJobs = mutableListOf<Job>()
                    val ballotProducer = produceBallots(decryptor, mixnetBallots, encryptedBallots)
                    repeat(nthreads) {
                        pepJobs.add(
                            launchPepWorker(
                                ballotProducer,
                                mixnetPep,
                                outputChannel
                            )
                        )
                    }
                    launchSink(outputChannel, sink)

                    // wait for all decryptions to be done, then close everything
                    joinAll(*pepJobs.toTypedArray())
                    outputChannel.close()
                }
            } finally {
                sink.close()
            }

            mixnetPep.stats.show(5)
            val count = mixnetPep.stats.count()

            val took = getSystemTimeInMillis() - starting
            val msecsPerBallot = (took.toDouble() / 1000 / count).sigfig()
            println("MixnetBlindTrustPep took ${took / 1000} wallclock secs for $count ballots = $msecsPerBallot secs/ballot with $nthreads threads")
        }

        // parallelize over ballots
        // place the ballot reading into its own coroutine
        @OptIn(ExperimentalCoroutinesApi::class)
        private fun CoroutineScope.produceBallots(
            decryptor: CiphertextDecryptor,
            mixnetBallots: List<MixnetBallot>,
            encryptedBallots: Map<Int, EncryptedBallot>): ReceiveChannel<Pair<EncryptedBallot, MixnetBallot>> =

            produce {
                mixnetBallots.forEachIndexed { idx, mixnetBallot ->
                    val first = decryptor.decryptPep(mixnetBallot.ciphertexts[0])
                    val match = encryptedBallots[first.hashCode()]
                    if (match == null) {
                        logger.warn { "Match ballot ${idx + 1} NOT FOUND" }
                    } else {
                        send(Pair(match, mixnetBallot.removeFirst()))
                    }
                    yield()
                }
                channel.close()
            }

        private fun CoroutineScope.launchPepWorker(
            input: ReceiveChannel<Pair<EncryptedBallot, MixnetBallot>>,
            pep: MixnetPepBlindTrust,
            output: SendChannel<BallotPep>,
        ) = launch(Dispatchers.Default) {

            for (ballotPair in input) {
                val (eballot, mixballot) = ballotPair
                val result = pep.testEquivalent(eballot, mixballot)
                if (result is Err) {
                    logger.warn { " PEP error on ballot ${eballot.ballotId} because $result" }
                } else {
                    val pepBallot = result.unwrap()
                    logger.info { " PEP compared ballotId='${pepBallot.ballotId}' equality=${pepBallot.isEq}" }
                    output.send(pepBallot)
                }
                yield()
            }
        }

        // place the output writing into its own coroutine
        private fun CoroutineScope.launchSink(
            input: Channel<BallotPep>, sink: PepBallotSinkIF,
        ) = launch {
            for (result in input) {
                sink.writePepBallot(result)
            }
        }
    }
}