package com.sunya.verificabitur.reader

import java.math.BigInteger

data class ModPGroupNode(val modulus : BigInteger, val order : BigInteger, val generator : BigInteger, val encoding : Int, )

fun readModPGroup(marsh : String) : ModPGroupNode {
    val root = readByteTree(marsh)
    require(root.className == "com.verificatum.arithm.ModPGroup")

    val modPGroupNode = root.root.children[1]
    require(modPGroupNode.n == 4)

    val modulus = BigInteger(1, modPGroupNode.children[0].content)
    val order = BigInteger(1, modPGroupNode.children[1].content)
    val generator = BigInteger(1, modPGroupNode.children[2].content)
    val encoding = readInt(modPGroupNode.children[3].content!!, 0)

    return ModPGroupNode(modulus, order, generator, encoding)
}