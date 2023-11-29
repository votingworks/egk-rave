package com.sunya.verificabitur.reader

import electionguard.rave.readByteTreeFromFile
import org.junit.jupiter.api.Test

class ByteTreeReaderTest {
    val demoDir = "/home/stormy/dev/verificatum-vmn-3.1.0-full/verificatum-vmn-3.1.0/demo/mixnet/mydemodir/"
    val raveDir = "/home/stormy/dev/github/electionguard-kotlin-multiplatform/egklib/src/commonTest/data/rave/vf/"
    val nizkpDir = "working/vf/dir/nizkp/98330134/proofs/"

    @Test
    fun testReadRaveOutput() {
        val root = readByteTreeFromFile(raveDir + "after-mix-2-ciphertexts.raw")
        println(root.show(10))
    }

    @Test
    fun testReadRaveInput() {
        val root = readByteTreeFromFile(raveDir + "src/commonTest/data/rave/vf/input-ciphertexts.raw")
        println(root.show(10))
    }

    @Test
    fun testReadDemoProofs() {
        readByteTreeFromFile(demoDir + "Party01/export/default/proofs/Ciphertexts01.bt", 1)
        readByteTreeFromFile(demoDir + "Party01/export/default/proofs/PermutationCommitment01.bt", 1)
        readByteTreeFromFile(demoDir + "Party01/export/default/proofs/PoSCommitment01.bt", 1)
    }

    @Test
    fun testReadPublicKeyFile() {
        val root = readPublicKeyFile(demoDir + "Party01/publicKey")
        println(root.show(10))
    }

    @Test
    fun testReadProofs() {
        readByteTreeFromFile(nizkpDir + "PermutationCommitment01.bt", 1)
        readByteTreeFromFile(nizkpDir + "PoSCommitment01.bt", 1)
        readByteTreeFromFile(nizkpDir + "PoSReply01.bt", 1)
    }

    @Test
    fun testReadProofCiphertexts() {
        readByteTreeFromFile(nizkpDir + "Ciphertexts01.bt", 2)
    }

}