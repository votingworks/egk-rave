package org.cryptobiotic.verificabitur.bytetree

import kotlin.test.Test

class ByteTreeWriterTest {
    val raveDir = "src/test/data/rave/vf"
    val nizkpDir = "working1/vf/dir/nizkp/1701230437"
    val nizkpDirProofs = "working1/vf/dir/nizkp/1701230437/proofs"

    val testOutDir = "testOut/ByteTreeWriterTest"

    @Test
    fun testRoundtripRavePublicKeyFile() {
        roundtrip(raveDir, "publickey.raw")
    }

    @Test
    fun testRoundtripPermutationCommitment() {
        roundtrip(nizkpDirProofs, "PermutationCommitment01.bt")
    }

    @Test
    fun testRoundtripPoSCommitment() {
        roundtrip(nizkpDirProofs, "PoSCommitment01.bt")
    }

    @Test
    fun testRoundtripPoSReply() {
        roundtrip(nizkpDirProofs, "PoSReply01.bt")
    }

    @Test
    fun testRoundtripCiphertexts() {
        roundtrip(nizkpDir, "Ciphertexts.bt", 2)
    }

    @Test
    fun testRoundtripShuffledCiphertexts() {
        roundtrip(nizkpDir, "ShuffledCiphertexts.bt", 1)
    }

    fun roundtrip(dir: String, filename : String, maxDepth: Int = 10) {
        val pathname = "$dir/$filename"
        println("readPublicKeyFile filename = $pathname")
        val tree = readByteTreeFromFile(pathname)
        println(tree.show(maxDepth))

        val writeFile = "$testOutDir/${filename}.roundtrip"
        writeByteTreeToFile(tree.root, writeFile)
        val roundtrip = readByteTreeFromFile(writeFile)
        println(roundtrip.show(maxDepth))

        compareFiles(pathname, writeFile)
    }

}