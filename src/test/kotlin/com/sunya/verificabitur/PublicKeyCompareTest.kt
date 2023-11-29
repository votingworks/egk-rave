package com.sunya.verificabitur

import com.github.michaelbull.result.unwrap
import com.sunya.verificabitur.reader.MixnetPublicKey
import com.sunya.verificabitur.reader.readPublicKey
import electionguard.core.*
import electionguard.publish.Consumer
import electionguard.publish.makeConsumer
import kotlin.test.Test
import kotlin.test.assertEquals

/** Compare ElectionGuard and Verificatum group definitions */
class PublicKeyCompareTest {
    val group = productionGroup()
    val raveDir = "working"

    @Test
    fun testComparePublicKey() {
        val filename = "$raveDir/vf/publickey.raw"
        println("readPublicKeyFile filename = ${filename}")
        val mpk: MixnetPublicKey = readPublicKey(filename, group)
        println( "MixnetPublicKey = \n${mpk}")

        val egdir = "$raveDir/eg"
        val consumer : Consumer = makeConsumer(group, egdir, true)
        val init = consumer.readElectionInitialized().unwrap()
        assertEquals(init.jointPublicKey, mpk.publicKey)
    }

}
