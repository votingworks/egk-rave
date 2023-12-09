package org.cryptobiotic

class TestFiles {
    companion object {
        val workingDir = "src/test/data/working"
        val bbDir =  "$workingDir/bb"
        val egDir =  "$workingDir/eg"
        val encryptedBallots = "$bbDir/encryptedBallots"
        val vfDir =  "$workingDir/vf"

        val trusteeDir =  "$egDir/trustees"
        val pballotsDir =  "$egDir/inputBallots"
        val mixedInput = "$bbDir/vf/mix1/Ciphertexts.bt"
        val mixedOutput = "$bbDir/vf/mix2/ShuffledCiphertexts.bt"
    }
}