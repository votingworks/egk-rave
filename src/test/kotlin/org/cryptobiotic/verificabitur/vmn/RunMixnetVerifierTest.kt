package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunMixnetVerifierTest {

    @Test
    fun testRunVerifier() {
        val inputDir = "working/vf"
        RunMixnetVerifier.main(
            arrayOf(
                "-nizkp", "$inputDir/dir/nizkp/1701230437",
                "-protInfo", "$inputDir/protInfo.xml",
                "-auxsid", "1701230437",
                "-width", "34",
            )
        )
        RunMixnetVerifier.main(
            arrayOf(
                "-nizkp", "$inputDir/dir/nizkp/1701230458",
                "-protInfo", "$inputDir/protInfo.xml",
                "-auxsid", "1701230458",
                "-width", "34",
            )
        )
    }

    @Test
    fun testRunVerifierMinimal() {
        val inputDir = "testVerifier"
        RunMixnetVerifier.main(
            arrayOf(
                "-nizkp", "$inputDir/mix1",
                "-protInfo", "$inputDir/protInfo.xml",
                "-auxsid", "mix1",
                "-width", "34",
            )
        )
        RunMixnetVerifier.main(
            arrayOf(
                "-nizkp", "$inputDir/mix2",
                "-protInfo", "$inputDir/protInfo.xml",
                "-auxsid", "mix2",
                "-width", "34",
            )
        )
    }
}