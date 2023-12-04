package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import electionguard.rave.CiphertextDecryptor
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class MixnetBallotReaderTest {
    val topDir = "working/vf"
    val nizkpDir1 = "$topDir/dir/nizkp/1701230437"
    val nizkpDir2 = "$topDir/dir/nizkp/1701230458"
    val egDir = "working/eg"
    val group = productionGroup()

    @Test
    fun testMixnetRawBallotTop() {
        readMixnetBallot("$topDir/input-ciphertexts.raw")
        readMixnetBallot("$topDir/after-mix-1-ciphertexts.raw")
        readMixnetBallot("$topDir/after-mix-2-ciphertexts.raw")
    }

    @Test
    fun testMixnetRawBallotNizkp1() {
        readMixnetBallot("$nizkpDir1/Ciphertexts.bt")
        readMixnetBallot("$nizkpDir1/ShuffledCiphertexts.bt")
        readMixnetBallot("$nizkpDir1/proofs/Ciphertexts01.bt")
    }

    @Test
    fun testMixnetRawBallotNizkp2() {
        readMixnetBallot("$nizkpDir2/Ciphertexts.bt")
        readMixnetBallot("$nizkpDir2/ShuffledCiphertexts.bt")
        readMixnetBallot("$nizkpDir2/proofs/Ciphertexts01.bt")
    }

    @Test
    fun showFiles() {
        showFileHash("$topDir/input-ciphertexts.raw")
        showFileHash("$nizkpDir1/Ciphertexts.bt")
        println()
        showFileHash("$nizkpDir1/ShuffledCiphertexts.bt")
        showFileHash("$nizkpDir1/proofs/Ciphertexts01.bt")
        showFileHash("$topDir/after-mix-1-ciphertexts.raw")
        showFileHash("$nizkpDir2/Ciphertexts.bt")
        println()

        showFileHash("$nizkpDir2/ShuffledCiphertexts.bt")
        showFileHash("$nizkpDir2/proofs/Ciphertexts01.bt")
        showFileHash("$topDir/after-mix-2-ciphertexts.raw")
    }


    @Test
    fun compareFiles() {
        compareFiles("$topDir/input-ciphertexts.raw", "$nizkpDir1/Ciphertexts.bt")
        compareFiles("$topDir/input-ciphertexts.raw", "$nizkpDir1/Ciphertexts.bt")

        compareFiles("$topDir/after-mix-1-ciphertexts.raw", "$nizkpDir1/proofs/Ciphertexts01.bt")
        compareFiles("$topDir/after-mix-2-ciphertexts.raw", "$nizkpDir2/proofs/Ciphertexts01.bt")

        compareFiles("$topDir/after-mix-1-ciphertexts.raw", "$nizkpDir1/ShuffledCiphertexts.bt")
        compareFiles("$topDir/after-mix-2-ciphertexts.raw", "$nizkpDir2/ShuffledCiphertexts.bt")
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

    fun compareFiles(file1 : String, file2 : String) {
        val ba1 = File(file1).readBytes()
        val ba2 = File(file2).readBytes()
        val same = ba1.contentEquals(ba2)
        println("$file1 (${ba1.contentHashCode()}) \n$file2 (${ba2.contentHashCode()}) \n same = $same \n")
    }

    fun showFileHash(file1 : String) {
        val ba1 = File(file1).readBytes()
        println("$file1 (${ba1.contentHashCode()})")
    }
}

/*
// input
working/vf/input-ciphertexts.raw (214181973)
working/vf/dir/nizkp/1701230437/Ciphertexts.bt (214181973)

// output of shuffle1
working/vf/dir/nizkp/1701230437/ShuffledCiphertexts.bt (1351194689)
working/vf/dir/nizkp/1701230437/proofs/Ciphertexts01.bt (1351194689)
working/vf/after-mix-1-ciphertexts.raw (1351194689)
working/vf/dir/nizkp/1701230458/Ciphertexts.bt (1351194689)

// output of shuffle2
working/vf/dir/nizkp/1701230458/ShuffledCiphertexts.bt (1587506439)
working/vf/dir/nizkp/1701230458/proofs/Ciphertexts01.bt (1587506439)
working/vf/after-mix-2-ciphertexts.raw (1587506439)
 */