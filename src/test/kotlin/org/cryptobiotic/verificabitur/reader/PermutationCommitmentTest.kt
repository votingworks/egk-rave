package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import org.junit.jupiter.api.Test

class PermutationCommitmentTest {
    val nizkpDir = "working/vf/dir/nizkp/98330134/proofs"
    val demoDir = "/home/stormy/dev/verificatum-vmn-3.1.0-full/verificatum-vmn-3.1.0/demo/mixnet/mydemodir"
    val group = productionGroup()


    @Test
    fun testReadPermutationCommitment1() {
        val pc = readPermutationCommitment("$nizkpDir/PermutationCommitment01.bt", group)
        println(pc.show())
    }

    @Test
    fun testReadPermutationCommitment3() {
        val pc = readPermutationCommitment("$demoDir/Party01/export/default/proofs/PermutationCommitment01.bt", group)
        println(pc.show())
    }
}