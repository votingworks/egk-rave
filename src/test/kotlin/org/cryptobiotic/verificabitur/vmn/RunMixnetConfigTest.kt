package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunMixnetConfigTest {
    val input = "working/eg"
    val working = "testOut/RunMixnetConfigTest"

    @Test
    fun testRunMixnetConfig() {
        RunMixnetConfig.main(
            arrayOf(
                "-input", input,
                "-working", working,
            )
        )
    }
}