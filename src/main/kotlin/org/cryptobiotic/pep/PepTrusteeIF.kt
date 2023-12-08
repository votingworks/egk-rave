package org.cryptobiotic.pep

import electionguard.core.ElGamalCiphertext

interface PepTrusteeIF {
    fun blind(
        texts: List<ElGamalCiphertext>,
    ): List<BlindResponse>

    fun challenge(
        challenges: List<BlindChallenge>,
    ): List<BlindChallengeResponse>
}