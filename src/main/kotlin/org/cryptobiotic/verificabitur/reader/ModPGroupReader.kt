package org.cryptobiotic.verificabitur.reader

import com.verificatum.arithm.LargeInteger
import com.verificatum.arithm.ModPGroup
import com.verificatum.crypto.RandomSource
import java.math.BigInteger

data class ModPGroupNode(val wtf: BigInteger, val modulus : BigInteger, val order : BigInteger, val generator : BigInteger, val encoding : Int, ) {
    override fun toString(): String {
        return  "      wtf = ${this.wtf.toString(16)}\n" +
                "  modulus = ${this.modulus.toString(16)}\n" +
                "    order = ${this.order.toString(16)}\n" +
                "generator = ${this.generator.toString(16)}\n" +
                " encoding = ${this.encoding.toString(16)}"
    }

    fun makeModPGroup(rs: RandomSource, certainty: Int) =
        ModPGroup(
            LargeInteger(modulus),
            LargeInteger(order),
            LargeInteger(generator),
            encoding,
            rs,
            certainty
        )
}

fun readModPGroupNode(marsh : String) : ModPGroupNode {
    val tree = readByteTree(marsh)
    require(tree.className == "com.verificatum.arithm.ModPGroup")

    val wtfNode = tree.root.child[0]
    val wtf = BigInteger(1, wtfNode.content)

    val modPGroupNode = tree.root.child[1]
    require(modPGroupNode.n == 4)

    val modulus = BigInteger(1, modPGroupNode.child[0].content)
    val order = BigInteger(1, modPGroupNode.child[1].content)
    val generator = BigInteger(1, modPGroupNode.child[2].content)
    val encoding = readInt(modPGroupNode.child[3].content!!, 0)

    return ModPGroupNode(wtf, modulus, order, generator, encoding)
}