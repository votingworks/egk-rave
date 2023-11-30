package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import electionguard.rave.CiphertextDecryptor
import org.cryptobiotic.verificabitur.reader.readMixnetBallot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MixnetBallotReaderTest {
    val nizkpDir = "working/vf/dir/nizkp/98330134/proofs/"
    val inputDir = "working/eg"
    val group = productionGroup()

    @Test
    fun testMixnetBallotReader() {
        val ballots = readMixnetBallot(nizkpDir + "Ciphertexts01.bt", group)
        assertEquals(13, ballots.size)
        ballots.forEach() {
            assertEquals(34, it.ciphertexts.size)
        }

        // the real test is if we can decrypt them
        val decryptor = CiphertextDecryptor(
            group,
            inputDir,
            "$inputDir/trustees",
        )
        ballots.forEachIndexed() { idx, it ->
            decryptor.decryptPep(it.encryptedSn())
            print("Ballot $idx decrypted to K^sn")
            it.removeFirst().ciphertexts.forEach { text ->
                decryptor.decrypt(text)
            }
            println(": all ciphertexts decrypted")
        }
    }
}