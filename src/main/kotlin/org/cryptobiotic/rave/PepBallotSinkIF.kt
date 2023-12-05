package org.cryptobiotic.rave

import com.cryptobiotic.rave.BallotPep
import electionguard.publish.Closeable

interface PepBallotSinkIF : Closeable {
    fun writePepBallot(pepBallot: BallotPep)
}