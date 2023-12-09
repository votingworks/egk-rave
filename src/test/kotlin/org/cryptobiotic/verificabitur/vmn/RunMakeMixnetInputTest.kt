package org.cryptobiotic.verificabitur.vmn

import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import org.cryptobiotic.verificabitur.vmn.RunMakeMixnetInput
import kotlin.test.Test
import kotlin.test.assertEquals

class RunMakeMixnetInputTest {
    val bbDir = "src/test/data/working/bb"

    @Test
    fun testMakeMixnetInputJson() {
        RunMakeMixnetInput.main(
            arrayOf(
                "-eballots", "$bbDir/encryptedBallots",
                "--outputFile", "testOut/inputCiphertexts.json",
                "-json"
            )
        )
    }

    @Test
    fun testMakeMixnetInput() {
        RunMakeMixnetInput.main(
            arrayOf(
                "-eballots", "$bbDir/encryptedBallots",
                "--outputFile", "testOut/inputCiphertexts.bt",
            )
        )
        // make sure its correctly formed
        val root = readByteTreeFromFile("testOut/inputCiphertexts.bt").root
        assertEquals(2, root.n)
        assertEquals(34, root.child[0].n)
        assertEquals(13, root.child[0].child[0].n)
        assertEquals(513, root.child[0].child[0].child[0].n)
    }

}

