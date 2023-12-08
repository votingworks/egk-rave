package org.cryptobiotic.pep

import electionguard.publish.Closeable

interface PepBallotSinkIF : Closeable {
    fun writePepBallot(pepBallot: BallotPep)
}