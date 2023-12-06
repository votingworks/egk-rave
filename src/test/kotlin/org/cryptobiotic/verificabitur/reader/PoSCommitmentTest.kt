package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PoSCommitmentTest {
    val nizkpDir = "working/vf/dir/nizkp/1701230458/proofs"
    val demoDir = "/home/stormy/dev/verificatum-vmn-3.1.0-full/verificatum-vmn-3.1.0/demo/mixnet/mydemodir"
    val group = productionGroup()

    @Test
    fun testReadPoSCommitment1() {
        val pc =  readPoSCommitment("$nizkpDir/PoSCommitment01.bt", group)
        println(pc.show())
    }

    @Test
    fun testReadPoSCommitment3() {
        val pc =  readPoSCommitment("$demoDir/Party01/export/default/proofs/PoSCommitment01.bt", group)
        println(pc.show())
        assertEquals(1, pc.Fp.size)
    }
}