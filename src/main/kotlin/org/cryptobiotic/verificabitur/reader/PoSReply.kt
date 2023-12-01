package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import java.math.BigInteger

// Algorithm 19
// PoSReply: σ^pos = node(kA,kB,kC,kD,kE,kF) where kA,kC,kD in Zq; kF in Rκ,ω; kB,kE are arrays of n elements in Zq.
data class PoSReply(
    val kA: ElementModQ,
    val kB: List<ElementModQ>,
    val kC: ElementModQ,
    val kD: ElementModQ,
    val kE: List<ElementModQ>,
    val kF: List<ElementModQ>,
) {
    fun show(): String{
        return buildString {
            appendLine("kA = ${kA}")
            append("kB = ")
            kB.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it}")
            }
            appendLine("kC = ${kC}")
            appendLine("kD = ${kD}")
            append("kE = ")
            kE.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it}")
            }
            append("kF = ")
            kF.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it}")
            }
        }
    }
}

fun readPoSReply(filename : String, group : GroupContext) : PoSReply {
    val tree = readByteTreeFromFile(filename)
    if (tree.className != null) println("readPoSReply class name = $tree.className")
    require(tree.root.childs() == 6)
    val kA = ProductionElementModQ(BigInteger(1, tree.root.child[0].content), group as ProductionGroupContext)
    val kB = readElementModQList(tree.root.child[1], group)
    val kC = ProductionElementModQ(BigInteger(1, tree.root.child[2].content), group)
    val kD = ProductionElementModQ(BigInteger(1, tree.root.child[3].content), group)
    val kE = readElementModQList(tree.root.child[4], group)
    val kF = readElementModQList(tree.root.child[5], group)

    return PoSReply(kA, kB, kC, kD, kE, kF)
}

fun readElementModQList(node: ByteTreeRoot.Node, group : GroupContext) : List<ElementModQ>{
    if (node.isLeaf) {
        val commit = ProductionElementModQ(BigInteger(1, node.content), group as ProductionGroupContext)
        return listOf(commit)
    }
    val n = node.childs()
    val commitments = mutableListOf<ElementModQ>()
    repeat(n) { idx ->
        val commit = ProductionElementModQ(BigInteger(1, node.child[idx].content), group as ProductionGroupContext)
        commitments.add(commit)
    }
    return commitments
}