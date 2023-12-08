package org.cryptobiotic.mixnet

import kotlin.test.Test

class RunMixnetBlindTrustPepTest {
    val topDir = "src/test/data/working/"
    val bbDir = "$topDir/bb"

    @Test
    fun testRunMixnetBlindTrustPep() {
        RunMixnetBlindTrustPep.main(
            arrayOf(
                "-in", "$bbDir/eg",
                "-eballots", "$bbDir/encryptedBallots",
                "--mixedBallots", "$bbDir/vf/mix2/ShuffledCiphertexts.bt",
                "-trustees", "$topDir/eg/trustees",
                "-out", "testOut/RunMixnetBlindTrustPepTest/",
                "-nthreads", "33",
            )
        )
    }

}

