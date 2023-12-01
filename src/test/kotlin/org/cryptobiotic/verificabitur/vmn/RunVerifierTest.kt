package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunVerifierTest {
    val inputDir = "working/vf"
    val outputDir = "testDir"

    @Test
    fun testRunVerifier() {
        RunVerifier.main(
            arrayOf(
                "-nizkp", "$inputDir/dir/nizkp/1701230458",
                "-protInfo", "$inputDir/protInfo.xml",
                "-auxsid", "1701230458",
                "-width", "34",
            )
        )
    }
}

/*
Usage: RunMixnet options_list
Options:
    --type, -type -> Mix type (always required) { Value should be one of [shuffle, decrypt, mix] }
    --input, -ciphertexts -> File of ciphertexts to be mixed (always required) { String }
    --output, -plaintexts -> Output file after mixing and/or decryption (always required) { String }
    --privInfo, -privInfo [privInfo.xml] -> Private info file { String }
    --protInfo, -protInfo [protInfo.xml] -> Protocol info file { String }
    --width, -width -> Number of ciphertexts per row (always required) { Int }
    --auxsid, -auxsid [default] -> Auxiliary session identifier used to distinguish different sessions of the mix-net { String }
    --help, -h -> Usage info
 */