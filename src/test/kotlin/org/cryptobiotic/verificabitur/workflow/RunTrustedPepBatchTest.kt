package org.cryptobiotic.verificabitur.workflow

import electionguard.cli.RunBatchEncryption
import org.cryptobiotic.TestFiles
import org.cryptobiotic.pep.RunMixnetBlindTrustPep
import org.cryptobiotic.pep.RunVerifyPep
import kotlin.test.Test

/** Test Decryption with in-process DecryptingTrustee's. */
class RunTrustedPepBatchTest {

    @Test
    fun testTrustedPepBatch() {
        val outputDir = "testOut/testTrustedPepBatch"
        val scannedDir = "$outputDir/scanned"
        val invalidDir = "$outputDir/invalid"

        // run the extra encryption
        RunBatchEncryption.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-ballots", TestFiles.pballotsDir,
                "-out", scannedDir,
                "-invalid", invalidDir,
                "-device", "scanned",
                "--cleanOutput",
            )
        )

        RunMixnetBlindTrustPep.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-trustees", TestFiles.trusteeDir,
                "-encryptedBallotsDir", "$scannedDir/encrypted_ballots/scanned/",
                "-encryptedBallotsDir", "$scannedDir/encrypted_ballots/scanned/",
                "-isJson",
                "-out", outputDir,
            )
        )

        RunVerifyPep.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-pep", outputDir,
            )
        )
    }

}
