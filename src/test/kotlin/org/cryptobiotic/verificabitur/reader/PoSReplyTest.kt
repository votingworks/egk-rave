package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PoSReplyTest {
    val nizkpDir = "working/vf/dir/nizkp/98330134/proofs"
    val demoDir = "/home/stormy/dev/verificatum-vmn-3.1.0-full/verificatum-vmn-3.1.0/demo/mixnet/mydemodir"
    val group = productionGroup()

    @Test
    fun testReadPoSReply1() {
        val pc =  readPoSReply("$nizkpDir/PoSReply01.bt", group)
        println(pc.show())
    }

    @Test
    fun testReadPoSReply3() {
        val pc =  readPoSReply("$demoDir/Party01/export/default/proofs/PoSReply01.bt", group)
        println(pc.show())
        assertEquals(1, pc.kF.size)
    }
}