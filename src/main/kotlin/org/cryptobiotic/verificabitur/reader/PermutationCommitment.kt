package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import java.math.BigInteger

data class PermutationCommitment(
    val commitments: List<ElementModP>
) {
    fun show(): String{
        return buildString {
            commitments.forEachIndexed { idx, it ->
                appendLine("${idx+1} ${it.toStringShort()}")
            }
        }
    }
}

fun readPermutationCommitment(filename : String, group : GroupContext) : PermutationCommitment{
    val tree = readByteTreeFromFile(filename)
    if (tree.className != null) println("class name = $tree.className")
    return PermutationCommitment(readElementModPList(tree.root, group))
}

fun readElementModPList(node: ByteTreeRoot.Node, group : GroupContext) : List<ElementModP>{
    val n = node.childs()
    val commitments = mutableListOf<ElementModP>()
    repeat(n) { idx ->
        val commit = ProductionElementModP(BigInteger(1, node.child[idx].content), group as ProductionGroupContext)
        commitments.add(commit)
    }
    return commitments
}