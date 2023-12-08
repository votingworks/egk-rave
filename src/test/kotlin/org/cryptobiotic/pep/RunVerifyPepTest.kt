package org.cryptobiotic.pep

import org.cryptobiotic.pep.RunVerifyPep
import kotlin.test.Test

class RunVerifyPepTest {

    @Test
    fun testRunVerifyPep() {
        RunVerifyPep.main(
            arrayOf(
                "-in", "src/commonTest/data/rave/eg",
                "--pepBallotDir", "src/commonTest/data/rave/bb/pep",
            )
        )
    }
}