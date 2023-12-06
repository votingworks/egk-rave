package org.cryptobiotic.verificabitur.reader

import org.cryptobiotic.rave.CiphertextDecryptor
import electionguard.core.*
import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import org.cryptobiotic.verificabitur.bytetree.writeByteTree
import org.junit.jupiter.api.Test
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
    }

    @Test
    fun testMixnetRawBallotTop() {
        roundtrip(topDir,"input-ciphertexts.raw")
        roundtrip(topDir,"after-mix-1-ciphertexts.raw")
        roundtrip(topDir,"after-mix-2-ciphertexts.raw")
    }

    @Test
    fun testMixnetRawBallotNizkp1() {
        roundtrip(nizkpDir1,"Ciphertexts.bt")
        roundtrip(nizkpDir1,"ShuffledCiphertexts.bt")
        roundtrip(nizkpDir1,"proofs/Ciphertexts01.bt")
    }

    @Test
    fun testMixnetRawBallotNizkp2() {
        roundtrip(nizkpDir2, "Ciphertexts.bt")
        roundtrip(nizkpDir2,"ShuffledCiphertexts.bt")
        roundtrip(nizkpDir2,"/proofs/Ciphertexts01.bt")
    }

    fun roundtrip(dir: String, filename : String, maxDepth: Int = 1) {
        val pathname = "$dir/$filename"
        println("readMixnetBallots filename = $pathname")
        val root = readByteTreeFromFile(pathname)
        println(root.show(maxDepth))

        val writeFile = "$testOutDir/${filename}.roundtrip"
        writeByteTree(root, writeFile)
        val roundtrip = readByteTreeFromFile(writeFile)
        println(roundtrip.show(maxDepth))

        compareFiles(pathname, writeFile)

        readMixnetBallot(writeFile)
    }

    fun readMixnetBallot(inputFilename: String) {
        val ballots = readMixnetBallot(inputFilename, group)
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
