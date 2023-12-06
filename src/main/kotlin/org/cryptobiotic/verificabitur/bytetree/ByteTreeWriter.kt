package org.cryptobiotic.verificabitur.bytetree

import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun writeByteTreeToFile(node: ByteTreeNode, filename: String) {
    FileOutputStream(filename).use { out ->
        node.write(out)
        out.close()
    }
}

fun ByteTreeNode.write(out: OutputStream) {
    if (isLeaf) out.write(1) else out.write (0)
    out.write(intToBytes(n))
    if (isLeaf) out.write(content!!) else child.forEach { it.write(out) }
}

fun ByteTreeNode.array(): ByteArray {
    val bos = ByteArrayOutputStream()
    this.write(bos)
    return bos.toByteArray()
}

fun intToBytes(i: Int): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(i).order(ByteOrder.BIG_ENDIAN).array()