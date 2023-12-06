package org.cryptobiotic.verificabitur.bytetree

import java.io.FileOutputStream

fun writeByteTree(tree: ByteTreeRoot, filename: String) {
    FileOutputStream(filename).use { out ->
        tree.root.write(out)
        out.close()
    }
}


fun ByteTreeNode.write(out: FileOutputStream) {
    if (isLeaf) out.write(1) else out.write (0)
    out.writeInt(n)
    if (isLeaf) out.write(content!!) else child.forEach { it.write(out) }
}

// big endian
fun FileOutputStream.writeInt(v : Int) {
    write((v ushr 24) and 0xFF)
    write((v ushr 16) and 0xFF)
    write((v ushr 8) and 0xFF)
    write((v) and 0xFF)
}