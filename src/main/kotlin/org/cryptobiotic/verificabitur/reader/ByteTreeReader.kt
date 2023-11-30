package org.cryptobiotic.verificabitur.reader

import electionguard.core.Base16.fromHex
import electionguard.core.Base16.toHex
import java.io.EOFException
import java.io.File

fun readTextLinesFromFile(filename : String, maxLines : Int = -1) {
    println("readTextTreeFromFile = ${filename}")

    var count = 0
    val file = File(filename)
    file.forEachLine {
        if (maxLines > 0 && count < maxLines) { // TODO LAME
            val tree = readByteTree(it)
            println(tree.show())
        }
        count++
    }
    println("total nlines = $count")
}

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
        val result = ByteTreeRoot(ByteArray(0))
        result.error = "Did not find a hex array"
        result.beforeDoubleColon = beforeDoubleColon
        return result
    }

    val result = ByteTreeRoot(byteArray)

    result.beforeDoubleColon = beforeDoubleColon
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
        val result = ByteTreeRoot(ByteArray(0))
        result.error = "Did not find a hex array"
        result.beforeDoubleColon = beforeDoubleColon
        return result
    }

    val result = ByteTreeRoot(byteArray)

    result.beforeDoubleColon = beforeDoubleColon
    if (result.root.child.size == 2) {
        val classNode = result.root.child[0]
        if (classNode.content != null) {
            result.className = String(classNode.content)
        }
    }
    return result
}

// A byte tree is either a leaf containing an array of bytes, or a node containing other byte trees
class ByteTreeRoot(byteArray : ByteArray) {
    var error: String? = null
    var beforeDoubleColon: String? = null
    var className: String? = null
    val root : Node = Node(byteArray, 0, "root")

    fun show(maxDepth: Int = 100): String {
        return buildString {
            if (error != null) {
                appendLine("error = $error")
            } else {
                if (beforeDoubleColon != null) appendLine("beforeDoubleColon = '$beforeDoubleColon'")
                if (className != null) appendLine("marshalled className = '$className'")
                append(root.show(Indent(0), maxDepth))
            }
        }
    }

    fun makeNode(ba: ByteArray, start: Int, name : String) : Node {
        return Node(ba, start, name)
    }

    inner class Node(ba: ByteArray, start: Int, val name : String) {
        val isLeaf: Boolean
        val n: Int
        val child = mutableListOf<Node>()
        val content: ByteArray?
        var size: Int = 5
        var nodeCount = 1

        init {
            if (ba.size == 0) {
                isLeaf = false
                n = 0
                content = null
            } else {
                if (start >= ba.size) {
                    throw RuntimeException("exceeded size")
                }
                if (ba[start] > 1) {
                    throw RuntimeException("not a ByteTree")
                }
                isLeaf = ba[start] == 1.toByte()
                n = readInt(ba, start + 1)
                if (n >= ba.size) {
                    throw RuntimeException("Illegal value for n = $n")
                }
                // println("$name $isLeaf $start $n")
                if (isLeaf) {
                    content = ByteArray(n) { ba[start + 5 + it] }
                    size += n
                } else {
                    content = null
                    var idx = start + 5
                    repeat(n) {
                        val child = makeNode(ba, idx, "$name-$nodeCount")
                        nodeCount++
                        this.child.add(child)
                        idx += child.size
                        this.size += child.size
                    }
                }
            }
        }

        fun childs() = child.size

        fun show(indent: Indent, maxDepth: Int = 100): String {
            return if (indent.level > maxDepth && nodeCount > 11) "" else {
                return buildString {
                    append("${indent}$name n=$n size=$size ")
                    if (isLeaf) {
                        appendLine("content='${content!!.toHex().lowercase()}'")
                    } else {
                        appendLine()
                        child.forEach { append(it.show(indent.incr(), maxDepth)) }
                    }
                }
            }
        }
    }
}

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

private const val nspaces : Int = 2
class Indent(val level: Int) {
    private val indent = makeBlanks(level * nspaces)

    override fun toString(): String {
        return indent
    }

    fun incr() = Indent(level+1)

    private fun makeBlanks(len: Int) : String {
        val blanks = StringBuilder(len)
        for (i in 0 until len) {
            blanks.append(" ")
        }
        return blanks.toString()
    }
}