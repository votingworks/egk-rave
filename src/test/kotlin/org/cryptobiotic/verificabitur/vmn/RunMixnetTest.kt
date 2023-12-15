package org.cryptobiotic.verificabitur.vmn

import kotlin.test.assertTrue

class RunMixnetTest {
    val working = "src/test/data/working/vf"

    // need to set up a clean directory
    // @Test
    fun testRunMixnet() {
        RunMixnet.main(
            arrayOf(
                "-in", "$working/inputCiphertexts.bt",
                "-privInfo", "$working/privateInfo.xml",
                "-protInfo", "$working/protocolInfo.xml",
                "-sessionId", "mix1",
            )
        )

        RunMixnet.main(
            arrayOf(
                "-in", "$working/nizkp/mix1/ShuffledCiphertexts.bt",
                "-privInfo", "$working/privateInfo.xml",
                "-protInfo", "$working/protocolInfo.xml",
                "-sessionId", "mix2",
            )
        )
        assertTrue(true)
    }
}