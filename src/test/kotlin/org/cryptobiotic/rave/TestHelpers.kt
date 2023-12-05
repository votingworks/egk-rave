@file:OptIn(ExperimentalCoroutinesApi::class)

package org.cryptobiotic.rave

import electionguard.core.*
import kotlin.test.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.random.Random
import kotlin.random.nextUInt


/** Verifies that two byte arrays are different. */
fun assertContentNotEquals(a: ByteArray, b: ByteArray, message: String? = null) {
    assertFalse(a.contentEquals(b), message)
}

fun generateRangeChaumPedersenProofKnownNonce(
    context: GroupContext
): ChaumPedersenRangeProofKnownNonce {
    return ChaumPedersenRangeProofKnownNonce(
        listOf(generateGenericChaumPedersenProof(context)),
    )
}

fun generateGenericChaumPedersenProof(context: GroupContext): ChaumPedersenProof {
    return ChaumPedersenProof(generateElementModQ(context), generateElementModQ(context),)
}

fun generateSchnorrProof(context: GroupContext): SchnorrProof {
    return SchnorrProof(
        generatePublicKey(context),
        generateElementModQ(context),
        generateElementModQ(context),
    )
}

fun generateCiphertext(context: GroupContext): ElGamalCiphertext {
    return ElGamalCiphertext(generateElementModP(context), generateElementModP(context))
}

fun generateHashedCiphertext(context: GroupContext): HashedElGamalCiphertext {
    return HashedElGamalCiphertext(generateElementModP(context), "what".encodeToByteArray(), generateUInt256(context), 42)
}

fun generateElementModQ(context: GroupContext): ElementModQ {
    return context.uIntToElementModQ(Random.nextUInt(134217689.toUInt()))
}

fun generateUInt256(context: GroupContext): UInt256 {
    return generateElementModQ(context).toUInt256safe();
}

fun generateElementModP(context: GroupContext) = context.uIntToElementModP(Random.nextUInt(1879047647.toUInt()))

fun generatePublicKey(group: GroupContext): ElementModP =
    group.gPowP(group.randomElementModQ())
