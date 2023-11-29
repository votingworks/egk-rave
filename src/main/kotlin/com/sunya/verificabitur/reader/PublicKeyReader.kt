package com.sunya.verificabitur.reader

import java.io.File

fun readPublicKeyFile(filename : String ) : ByteTreeRoot {
    println("readPublicKeyFile filename = ${filename}")

    // gulp the entire file to a byte array
    val file = File(filename)
    val ba : ByteArray = file.readBytes()
    val tree = readByteTree(ba)
    println("readPublicKeyFile\n" + tree.show())
    return tree
}

/*

 */