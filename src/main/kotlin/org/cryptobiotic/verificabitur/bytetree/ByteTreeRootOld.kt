package org.cryptobiotic.verificabitur.bytetree

import electionguard.core.Base16.toHex
import electionguard.util.Indent
import java.io.File

fun readByteTreeOldFromFile(filename : String) : ByteTreeRootOld {
    val file = File(filename) // gulp the entire file to a byte array
    val ba : ByteArray = file.readBytes()
    return ByteTreeRootOld(ba)
}

// A byte tree is either a leaf containing an array of bytes, or a node containing other byte trees
class ByteTreeRootOld(byteArray : ByteArray) {
    var error: String? = null
    var beforeDoubleColon: String? = null
    var className: String? = null
    val root = NodeOld(byteArray, 0, "root")

    fun makeNode(ba: ByteArray, start: Int, name : String) : NodeOld {
        return NodeOld(ba, start, name)
    }

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

    inner class NodeOld(ba: ByteArray, start: Int, val name : String) {
        val isLeaf: Boolean
        val n: Int
        val child = mutableListOf<NodeOld>()
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
            println("  isLeaf $isLeaf n $n totalBytes $size ($name)")

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