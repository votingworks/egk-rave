package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import org.cryptobiotic.verificabitur.bytetree.ByteTreeNode
import org.cryptobiotic.verificabitur.bytetree.ByteTreeRoot
import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import java.math.BigInteger

// Algorithm 19
// PoSCommitment: τ^pos = node(B,A',B',C',D',F') where A',C',D' in Gq; F' in Cκ,ω; B,B' are arrays of n elements in Gq.
data class PoSCommitment(
    val B: List<ElementModP>,
    val Ap: ElementModP,
    val Bp: List<ElementModP>,
    val Cp: ElementModP,
    val Dp: ElementModP,
    val Fp: List<ElGamalCiphertext>,
) {
    fun show(): String{
        return buildString {
            append("B = ")
            B.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it.toStringShort()}")
            }
            appendLine("Ap = ${Ap.toStringShort()}")
            append("Bp = ")
            Bp.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it.toStringShort()}")
            }
            appendLine("Cp = ${Cp.toStringShort()}")
            appendLine("Dp = ${Dp.toStringShort()}")
            append("Fp = ")
            Fp.forEachIndexed { idx, it ->
                appendLine("   ${idx+1} ${it}")
            }
        }
    }
}

fun readPoSCommitment(filename : String, group : GroupContext) : PoSCommitment {
    val tree = readByteTreeFromFile(filename)
    if (tree.className != null) println("readPoSCommitment class name = $tree.className")
    require(tree.root.childs() == 6)
    val B = readElementModPList(tree.root.child[0], group)
    val Ap = ProductionElementModP(BigInteger(1, tree.root.child[1].content), group as ProductionGroupContext)
    val Bp = readElementModPList(tree.root.child[2], group)
    val Cp = ProductionElementModP(BigInteger(1, tree.root.child[3].content), group as ProductionGroupContext)
    val Dp = ProductionElementModP(BigInteger(1, tree.root.child[4].content), group as ProductionGroupContext)
    val Fp = readCiphertextList(tree.root.child[5], group)

    return PoSCommitment(B, Ap, Bp, Cp, Dp, Fp)
}

fun readCiphertextList(node: ByteTreeNode, group : GroupContext) : List<ElGamalCiphertext>{
    require(node.childs() == 2)
    val pads = node.child[0]
    val datas = node.child[1]

    // when only one, its a leaf
    require(pads.isLeaf == datas.isLeaf)
    if (pads.isLeaf) {
        val pad = ProductionElementModP(BigInteger(1, pads.content), group as ProductionGroupContext)
        val data = ProductionElementModP(BigInteger(1, datas.content), group)
        return listOf(ElGamalCiphertext(pad,data))
    }

    // else its a node
    require(pads.childs() == datas.childs())
    val ciphertexts = mutableListOf<ElGamalCiphertext>()
    repeat(pads.childs()) { idx ->
        val pad = ProductionElementModP(BigInteger(1, pads.child[idx].content), group as ProductionGroupContext)
        val data = ProductionElementModP(BigInteger(1, datas.child[idx].content), group)
        ciphertexts.add(ElGamalCiphertext(pad,data))
    }
    return ciphertexts
}