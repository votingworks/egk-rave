package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.pep.CiphertextDecryptor
import electionguard.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MixnetBallotWriterTest {
    val inputDir = "src/test/data/working/vf"
    val bbDir = "src/test/data/working/bb/vf"
    val nizkpDir = "$inputDir/Party01/nizkp"
    val proofsDir = "$inputDir/Party01/nizkp/mix2/proofs"

    val nizkpDir1 = "$nizkpDir/mix1"
    val nizkpDir2 = "$nizkpDir/mix2"
    val egDir = "working/eg"
    val group = productionGroup()

    val testOutDir = "testOut/MixnetBallotWriterTest"

    @Test
    fun testMixnetRoundtrip() {
        roundtrip(inputDir, "inputCiphertexts.bt")
        roundtrip(nizkpDir1,"ShuffledCiphertexts.bt")
        roundtrip(nizkpDir2,"ShuffledCiphertexts.bt")
    }

    fun roundtrip(dir: String, filename : String, maxDepth: Int = 1) {
        val pathname = "$dir/$filename"
        println("readMixnetBallots filename = $pathname")
        val ballots = readMixnetBallotFromFile(group, pathname)

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
        val ballots = readMixnetBallotFromFile(group, inputFilename)
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
