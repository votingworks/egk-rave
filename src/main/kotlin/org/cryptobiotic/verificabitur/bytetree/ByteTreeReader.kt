package org.cryptobiotic.verificabitur.bytetree

import electionguard.core.Base16.fromHex
import java.io.EOFException
import java.io.File

fun readByteTreeFromFile(filename : String) : ByteTreeRoot {
    val file = File(filename) // gulp the entire file to a byte array
    val ba : ByteArray = file.readBytes()
    return readByteTree(ba)
}

fun readByteTree(marsh : String) : ByteTreeRoot {
    var beforeDoubleColon : String? = null
    val byteArray : ByteArray? = if (marsh.contains("::")) {
        val frags = marsh.split("::")
        // frags.forEach { println(it) }
        beforeDoubleColon = frags[0]
        frags[1].fromHex()
    } else {
        marsh.fromHex()
    }
    if (byteArray == null) {
        return makeEmptyTree(beforeDoubleColon, "Did not find a hex array")
    }

    val result = makeTree(byteArray, beforeDoubleColon)

    if (result.root.child.size == 2) {
        val classNode = result.root.child[0]
        if (classNode.content != null) { // && is UTF
            result.className = String(classNode.content)
        }
    }
    return result
}

val COLON = ':'.code.toByte()
fun readByteTree(ba : ByteArray) : ByteTreeRoot {
    var split = -1
    for (idx in 0..100) {
        if (ba[idx] == COLON && ba[idx+1] == COLON) {
            split = idx
        }
    }

    var beforeDoubleColon : String? = null
    var byteArray : ByteArray? = if (split > 0) {
        val beforeBytes = ByteArray(split) { ba[it] }
        beforeDoubleColon = String(beforeBytes)
        val remaining = ba.size - (split + 2)
        ByteArray(remaining) { ba[it + split + 2] }
    } else {
        ba
    }
    if (byteArray == null) {
        return makeEmptyTree(beforeDoubleColon,"Did not find a hex array")
    }

    val result = makeTree(byteArray, beforeDoubleColon)
    if (result.root.child.size == 2) {
        val classNode = result.root.child[0]
        if (classNode.content != null) {
            result.className = String(classNode.content)
        }
    }
    return result
}

// big endian
fun readInt(ba : ByteArray, start : Int) : Int {
    val ch1: Int = ba[start].toInt()
    val ch2: Int = ba[start+1].toInt()
    val ch3: Int = ba[start+2].toInt()
    val ch4: Int = ba[start+3].toInt()
    if (ch1 or ch2 or ch3 or ch4 < 0) {
        throw EOFException()
    }
    return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + ch4
}