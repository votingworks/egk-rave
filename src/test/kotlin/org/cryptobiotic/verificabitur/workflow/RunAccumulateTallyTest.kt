package org.cryptobiotic.verificabitur.workflow

import electionguard.cli.RunAccumulateTally
import org.cryptobiotic.TestFiles
import kotlin.test.Test

class RunAccumulateTallyTest {

    @Test
    fun testAccumulateTally() {
        RunAccumulateTally.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-eballots", TestFiles.encryptedBallots,
                "-out", "testOut/testAccumulateTally"
            )
        )
    }

}

