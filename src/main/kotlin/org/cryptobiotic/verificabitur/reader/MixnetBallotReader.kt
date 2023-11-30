package org.cryptobiotic.verificabitur.reader

import electionguard.core.*
import java.math.BigInteger

// converts raw bytetrees to List<MixnetBallot>
data class MixnetBallot(
    val ciphertexts: List<ElGamalCiphertext>
) {
    fun removeFirst() : MixnetBallot = MixnetBallot(ciphertexts.subList(1, ciphertexts.size))
    fun encryptedSn() : ElGamalCiphertext = ciphertexts[0]

    fun show(): String{
        return buildString {
            ciphertexts.forEachIndexed { idx, it ->
                appendLine("${idx+1} $it")
            }
        }
    }
}

// readByteTreeFromFile = working/vf/dir/nizkp/98330134/proofs/Ciphertexts01.bt
//root n=2 size=458267
//  root-1 n=34 size=229131
//    root-1-1 n=13 size=6739
//      root-1-1-1 n=513 size=518 content='0078d59129...1de6bda1b5' ...
fun readMixnetBallot(filename : String, group : GroupContext) : List<MixnetBallot> {
    val tree = readByteTreeFromFile(filename)
    if (tree.className != null) println("class name = $tree.className")

    require(tree.root.child.size == 2)
    val padChildren = tree.root.child[0].child
    val dataChildren = tree.root.child[1].child
    require(padChildren.size == dataChildren.size)
    val ntexts = padChildren.size

    val listOfList = mutableListOf<List<ElGamalCiphertext>>()
    var nballots = 0
    repeat(ntexts) { textidx ->
        val ciphertexts = mutableListOf<ElGamalCiphertext>()
        val pads = padChildren[textidx]
        val datas = dataChildren[textidx]
        require(pads.childs() == datas.childs())
        if (nballots == 0) nballots = pads.childs() else { require(nballots == pads.childs() ) }

        repeat(pads.childs()) { idx ->
            val pad = ProductionElementModP(BigInteger(1, pads.child[idx].content), group as ProductionGroupContext)
            val data = ProductionElementModP(BigInteger(1, datas.child[idx].content), group)
            ciphertexts.add(ElGamalCiphertext(pad, data))
        }
        listOfList.add(ciphertexts)
    }

    // invert the listOfList from text,ballot to ballot,text
    val ballots = mutableListOf<MixnetBallot>()
    repeat(nballots) { ballotIdx ->
        val ciphertexts = mutableListOf<ElGamalCiphertext>()
        listOfList.forEach { clist ->
            ciphertexts.add(clist[ballotIdx])
        }
        ballots.add(MixnetBallot(ciphertexts))
    }

    return ballots
}