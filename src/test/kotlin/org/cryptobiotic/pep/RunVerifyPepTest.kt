package org.cryptobiotic.pep

import kotlin.test.Test

class RunVerifyPepTest {
    val topDir = "src/test/data/working/"
    val bbDir = "$topDir/bb"

    @Test
    fun testRunVerifyPep() {
        RunVerifyPep.main(
            arrayOf(
                "-in", "$bbDir/eg",
                "--pepBallotDir", "$bbDir/pep",
            )
        )
    }
}