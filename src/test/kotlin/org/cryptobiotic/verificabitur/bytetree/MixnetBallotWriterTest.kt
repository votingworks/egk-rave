package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.rave.CiphertextDecryptor
import electionguard.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MixnetBallotWriterTest {
    val topDir = "working1/vf"
    val nizkpDir1 = "$topDir/dir/nizkp/1701230437"
    val nizkpDir2 = "$topDir/dir/nizkp/1701230458"
    val egDir = "working/eg"
    val group = productionGroup()

    val testOutDir = "testOut/MixnetBallotWriterTest"

    @Test
    fun testMixnetRoundtrip() {
        roundtrip(topDir, "input-ciphertexts.raw")
        roundtrip(nizkpDir1,"ShuffledCiphertexts.bt")
        roundtrip(nizkpDir2,"ShuffledCiphertexts.bt")
    }

    fun roundtrip(dir: String, filename : String, maxDepth: Int = 1) {
        val pathname = "$dir/$filename"
        println("readMixnetBallots filename = $pathname")
        val ballots = readMixnetBallotFromFile(pathname, group)

        val tree = ballots.publish()
        println(tree.show())

        val writeFile = "$testOutDir/${filename}.roundtrip"
        writeByteTreeToFile(tree, writeFile)
        val roundtrip = readByteTreeFromFile(writeFile)
        println(roundtrip.show(maxDepth))

        compareFiles(pathname, writeFile)

        readAndDecryptMixnetBallot(writeFile)
    }

    fun readAndDecryptMixnetBallot(inputFilename: String) {
        val ballots = readMixnetBallotFromFile(inputFilename, group)
        assertEquals(13, ballots.size)
        ballots.forEach() {
            assertEquals(34, it.ciphertexts.size)
        }

        // the real test is if we can decrypt them
        val decryptor = CiphertextDecryptor(
            group,
            egDir,
            "$egDir/trustees",
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
