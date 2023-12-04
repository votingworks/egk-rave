package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunMixnetTest {
    val input = "testOut/runMixnetTest/input"
    val working = "testOut/runMixnetTest/output"

    @Test
    fun testRunMixnet() {
        RunMixnet.main(
            arrayOf(
                "-in", "$input/input-ciphertexts.bt",
                "-privInfo", "$input/privInfo.xml",
                "-protInfo", "$input/protInfo.xml",
                "-sessionId", "mix1",
            )
        )
        RunMixnet.main(
            arrayOf(
                "-in", "$working/nizkp/mix1/ShuffledCiphertexts.bt",
                "-privInfo", "$input/privInfo.xml",
                "-protInfo", "$input/protInfo.xml",
                "-sessionId", "mix2",
            )
        )
    }
}