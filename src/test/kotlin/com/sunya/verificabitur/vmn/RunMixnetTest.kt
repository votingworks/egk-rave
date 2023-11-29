package com.sunya.verificabitur.vmn

import kotlin.test.Test

class RunMixnetTest {
    val inputDir = "working/vf"
    val outputDir = "testDir"

    @Test
    fun testRunMixnet() {
        RunMixnet.main(
            arrayOf(
                "-type", "shuffle",
                "-ciphertexts", "$inputDir/input-ciphertexts.raw",
                "-privInfo", "$inputDir/privInfo.xml",
                "-protInfo", "$inputDir/protInfo.xml",
                "--output", "$outputDir/output-ciphertexts.raw",
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