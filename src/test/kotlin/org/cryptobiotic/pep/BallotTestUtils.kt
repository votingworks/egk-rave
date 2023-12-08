package org.cryptobiotic.pep

import com.github.michaelbull.result.*
import electionguard.ballot.EncryptedTally
import electionguard.ballot.Guardian
import electionguard.core.ElGamalCiphertext
import electionguard.core.UInt256
import electionguard.decrypt.DecryptingTrusteeDoerre
import electionguard.keyceremony.KeyCeremonyTrustee

fun makeDoerreTrustee(ktrustee: KeyCeremonyTrustee): DecryptingTrusteeDoerre {
    return DecryptingTrusteeDoerre(
        ktrustee.id,
        ktrustee.xCoordinate,
        ktrustee.guardianPublicKey(),
        ktrustee.computeSecretKeyShare(),
    )
}

fun makeGuardian(trustee: KeyCeremonyTrustee): Guardian {
    val publicKeys = trustee.publicKeys().unwrap()
    return Guardian(
        trustee.id,
        trustee.xCoordinate,
        publicKeys.coefficientProofs,
    )
}

fun makeTallyForSingleCiphertext(ciphertext : ElGamalCiphertext, extendedBaseHash : UInt256) : EncryptedTally {
    val selection = EncryptedTally.Selection("Selection1", 1, ciphertext)
    val contest = EncryptedTally.Contest("Contest1", 1, listOf(selection))
    return EncryptedTally("tallyId", listOf(contest), emptyList(), extendedBaseHash)
}
