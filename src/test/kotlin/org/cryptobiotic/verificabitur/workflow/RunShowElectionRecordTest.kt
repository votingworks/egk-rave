package org.cryptobiotic.verificabitur.workflow

import electionguard.cli.RunShowElectionRecord
import org.cryptobiotic.TestFiles
import kotlin.test.Test

class RunShowElectionRecordTest {

    @Test
    fun testShowElectionRecordTest() {
        RunShowElectionRecord.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-show", "manifest",
            )
        )
    }

    @Test
    fun testShowElectionRecordDetailsTest() {
        RunShowElectionRecord.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-show", "manifest",
                "--details",
            )
        )
    }

    @Test
    fun testShowElectionRecordBallotStyleTest() {
        RunShowElectionRecord.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-show", "manifest",
                "-ballotStyle", "ballot-style-1",
                "--details",
            )
        )
    }

}

