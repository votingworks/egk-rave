package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunMixnetTest {
    val working = "src/test/data/working/vf"

    @Test
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
    }
}