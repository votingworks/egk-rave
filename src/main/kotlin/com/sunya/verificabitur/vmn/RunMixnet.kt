package com.sunya.verificabitur.vmn

import com.verificatum.arithm.PGroup
import com.verificatum.arithm.PGroupElementArray
import com.verificatum.eio.ExtIO
import com.verificatum.protocol.Protocol
import com.verificatum.protocol.ProtocolError
import com.verificatum.protocol.ProtocolFormatException
import com.verificatum.protocol.elgamal.ProtocolElGamal
import com.verificatum.protocol.elgamal.ProtocolElGamalInterface
import com.verificatum.protocol.elgamal.ProtocolElGamalInterfaceFactory
import com.verificatum.protocol.mixnet.MixNetElGamal
import com.verificatum.protocol.mixnet.MixNetElGamalInterfaceFactory
import com.verificatum.ui.UI
import com.verificatum.ui.tui.TConsole
import com.verificatum.ui.tui.TextualUI
import com.verificatum.util.SimpleTimer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import kotlin.random.Random

private val logger = KotlinLogging.logger("RunMixnet")

class RunMixnet {

    // Usage:
    //vmn -h
    //vmn -keygen
    //[-cerr] [-e] [-s]
    //<privInfo> <protInfo> <publicKey>

    //vmn -mix
    //[-auxsid <sid>] [-cerr] [-e] [-maxciph <value>] [-s] [-width <value>]
    //<privInfo> <protInfo> <ciphertexts> <plaintexts>

    //vmn -delete
    //[-auxsid <sid>] [-cerr] [-e] [-f] [-s]
    //<privInfo> <protInfo>

    //vmn -lact
    //<privInfo> <protInfo>

    //vmn -sact
    //<privInfo> <protInfo> <indices>

    //vmn -precomp
    //[-auxsid <sid>] [-cerr] [-e] [-maxciph <value>] [-s] [-width <value>]
    //<privInfo> <protInfo>

    //vmn -setpk
    //[-cerr] [-e] [-s]
    //<privInfo> <protInfo> <publicKey>

    //vmn -version

    //////////////////////////////////////////////////
    // this is whats handled here

    //vmn -shuffle [-auxsid <sid>] [-cerr] [-e] [-s] [-width <value>]
    // <privInfo> <protInfo> <ciphertexts> <ciphertextsout>

    //vmn -decrypt  [-auxsid <sid>] [-cerr] [-e] [-s] [-width <value>]
    // <privInfo> <protInfo> <ciphertexts> <plaintexts>

    //vmn -mix [- <sid>] [-cerr] [-e] [-maxciph <value>] [-s] [-width <value>]
    // <privInfo> <protInfo> <ciphertexts> <plaintexts>
    //
    // shuffle and decrypt the input ciphertexts, i.e., the output is a list of randomly permuted plaintexts.

    /////////////////////////////////////////////////////////////////////////////////////////
    // rave_print "... Set up the mixnet, now loading encrypted ballots ..."
    //
    //# TODO
    // WIDTH=34
    //
    //# convert ciphertexts to V raw format
    // vmnc -e -ciphs -width "${WIDTH}"  -ini seqjson -outi raw \
    //     ${VERIFICATUM_WORKSPACE}/protInfo.xml ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json ${VERIFICATUM_WORKSPACE}/input-ciphertexts.raw
    //
    //
    //rave_print "... now shuffling once ..."
    //
    //AUXSID=`date "+%s" | sed "s/ /_/g"`
    //
    //# shuffle once
    // vmn -shuffle -width "${WIDTH}" -auxsid "${AUXSID}" \
    //    ${VERIFICATUM_WORKSPACE}/privInfo.xml \
    //    ${VERIFICATUM_WORKSPACE}/protInfo.xml \
    //    ${VERIFICATUM_WORKSPACE}/input-ciphertexts.raw ${VERIFICATUM_WORKSPACE}/after-mix-1-ciphertexts.raw
    //
    //rave_print "... and shuffling twice ..."
    //
    //AUXSID=`date "+%s" | sed "s/ /_/g"`
    //
    //# shuffle twice
    // vmn -shuffle -width "${WIDTH}" -auxsid "${AUXSID}" \
    //    ${VERIFICATUM_WORKSPACE}/privInfo.xml \
    //    ${VERIFICATUM_WORKSPACE}/protInfo.xml \
    //    ${VERIFICATUM_WORKSPACE}/after-mix-1-ciphertexts.raw ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.raw
    //
    //# convert output ciphertexts to JSON format
    // vmnc -ciphs -width "${WIDTH}" -ini raw -outi seqjson \
    //     ${VERIFICATUM_WORKSPACE}/protInfo.xml ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.raw ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.json
    //
    //rave_print "[DONE] Shuffled encrypted ballots are in ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.json"

    enum class MixnetType { shuffle, decrypt, mix }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser("RunMixnet")
            val type by parser.option(
                ArgType.Choice<MixnetType>(),
                shortName = "type",
                description = "Mix type"
            ).required()
            val input by parser.option(
                ArgType.String,
                shortName = "ciphertexts",
                description = "File of ciphertexts to be mixed"
            ).required()
            val output by parser.option(
                ArgType.String,
                shortName = "plaintexts",
                description = "Output file after mixing and/or decryption"
            ).required()
            val privInfo by parser.option(
                ArgType.String,
                shortName = "privInfo",
                description = "Private info file"
            ).default("privInfo.xml")
            val protInfo by parser.option(
                ArgType.String,
                shortName = "protInfo",
                description = "Protocol info file"
            ).default("protInfo.xml")
            val width by parser.option(
                ArgType.Int,
                shortName = "width",
                description = "Number of ciphertexts per row"
            ).required()
            val auxsid by parser.option(
                ArgType.String,
                shortName = "auxsid",
                description = "Auxiliary session identifier used to distinguish different sessions of the mix-net"
            )

            parser.parse(args)

            println(
                "RunMixnet starting\n" +
                        "   type= $type\n" +
                        "   input= $input\n" +
                        "   output = $output\n" +
                        "   privInfo = $privInfo\n" +
                        "   protInfo = $protInfo\n" +
                        "   width = $width\n" +
                        "   auxsid = $auxsid\n"
            )

            val mixnet = Mixnet(privInfo, protInfo)
            val sessionId = mixnet.run(type, input, output, auxsid, width)
            println("sessionId $sessionId complete successfully")
        }
    }
}


class Mixnet(privInfo: String, protInfo: String) {
    val elGamalRawInterface: ProtocolElGamalInterface
    val mixnet: MixNetElGamal
    var timer: SimpleTimer? = null

    init {
        val factory: ProtocolElGamalInterfaceFactory = MixNetElGamalInterfaceFactory()

        try {
            elGamalRawInterface = factory.getInterface("raw")
        } catch (pfe: ProtocolFormatException) {
            throw ProtocolError("Unable to get raw interface!", pfe)
        }

        val protocolInfoFile = File(protInfo)

        val generator = factory.getGenerator(protocolInfoFile)
        val privateInfo = Protocol.getPrivateInfo(generator, File(privInfo))
        val protocolInfo = Protocol.getProtocolInfo(generator, protocolInfoFile)

        val ui: UI = TextualUI(TConsole())
        mixnet = MixNetElGamal(privateInfo, protocolInfo, ui)
    }

    fun run(type: RunMixnet.MixnetType, input: String, output: String, auxsid: String?, width: Int): String {
        val inputCiphFile = File(input)
        val outputFile = File(output)
        val inputCiphertexts = readCiphertexts(mixnet, width, inputCiphFile)
        val sessionId = auxsid?: Random.nextInt(Int.MAX_VALUE).toString()

        try {
            when (type) {
                RunMixnet.MixnetType.shuffle -> processShuffle(sessionId, mixnet, outputFile, width, inputCiphertexts)
                RunMixnet.MixnetType.decrypt -> processDecrypt(sessionId, mixnet, outputFile, width, inputCiphertexts)
                RunMixnet.MixnetType.mix -> processMixing(sessionId, mixnet, outputFile, width, inputCiphertexts)
            }
        } catch (t: Throwable) {
            logger.error { "Exception= ${t.message} ${t.stackTraceToString()}" }
            t.printStackTrace()
        }
        return sessionId
    }

    internal fun readCiphertexts(mixnet: MixNetElGamal, width: Int, inputCiphFile: File?): PGroupElementArray {
        val ciphPGroup: PGroup = ProtocolElGamal.getCiphPGroup(mixnet.keyPGroup, width)

        val ciphertexts = elGamalRawInterface.readCiphertexts(ciphPGroup, inputCiphFile)
        if (ciphertexts.size() == 0) {
            val e = "No valid ciphertexts were found!"
            throw ProtocolFormatException(e)
        }
        return ciphertexts
    }

    private fun processShuffle(
        auxsidString: String,
        mixnet: MixNetElGamal,
        outputCiphFile: File,
        width: Int,
        inputCiphertexts: PGroupElementArray
    ) {
        prelude(mixnet)

        if (mixnet.readBoolean(".keygen")) {
            mixnet.generatePublicKey()
        }
        val session = mixnet.getSession(auxsidString)

        val outputCiphertexts = session.shuffle(width, inputCiphertexts)
        elGamalRawInterface.writeCiphertexts(outputCiphertexts, outputCiphFile)
        // inputCiphertexts.free();
        outputCiphertexts.free()

        postlude(mixnet, "shuffling")
    }

    private fun processMixing(
        auxsidString: String,
        mixnet: MixNetElGamal,
        plainFile: File,
        width: Int,
        inputCiphertexts: PGroupElementArray,
    ) {
        prelude(mixnet)

        mixnet.generatePublicKey()
        val session = mixnet.getSession(auxsidString)
        val plaintexts = session.mix(width, inputCiphertexts)
        elGamalRawInterface.decodePlaintexts(plaintexts, plainFile)

        inputCiphertexts.free()
        plaintexts.free()

        postlude(mixnet, "mixing")
    }

    private fun processDecrypt(
        auxsidString: String,
        mixnet: MixNetElGamal,
        plainFile: File,
        width: Int,
        inputCiphertexts: PGroupElementArray,
    ) {
        prelude(mixnet)

        mixnet.generatePublicKey()
        val session = mixnet.getSession(auxsidString)
        val plaintexts = session.decrypt(width, inputCiphertexts)
        elGamalRawInterface.decodePlaintexts(plaintexts, plainFile)

        inputCiphertexts.free()
        plaintexts.free()

        postlude(mixnet, "decryption")
    }

    private fun prelude(mixnet: MixNetElGamal) {
        mixnet.startServers()
        timer = SimpleTimer()
        mixnet.setup()
    }

    private fun postlude(
        mixnet: MixNetElGamal,
        timerString: String?
    ) {
        mixnet.shutdown(mixnet.log)

        val hline =
            "-----------------------------------------------------------"
        mixnet.log.plainInfo(hline)

        mixnet.log.plainInfo(
            String.format(
                "Completed %s.%n",
                timerString
            )
        )


        val totalExecutionTime = timer!!.elapsed()
        val totalNetworkTime = mixnet.totalNetworkTime
        val totalEffectiveTime = totalExecutionTime - totalNetworkTime
        val totalWaitingTime = mixnet.totalWaitingTime
        val totalCompTime = totalEffectiveTime - totalWaitingTime

        val sentBytes = mixnet.sentBytes
        val hSentBytes = ExtIO.bytesToHuman(sentBytes)

        val receivedBytes = mixnet.receivedBytes
        val hReceivedBytes = ExtIO.bytesToHuman(receivedBytes)

        val totalBytes = sentBytes + receivedBytes
        val hTotalBytes = ExtIO.bytesToHuman(totalBytes)

        val format = StringBuilder()
        format.append("Running time:    %13s                 %12s%n")
        format.append("- Execution      %13s                 %12d%n")
        format.append("- Network        %13s                 %12d%n")
        format.append("- Effective      %13s                 %12d%n")
        format.append("- Idle           %13s                 %12d%n")
        format.append("- Computation    %13s                 %12d%n")
        format.append("%n")
        format.append("Communication:   %13s                 %12s%n")
        format.append("- Sent           %13s                 %12d%n")
        format.append("- Received       %13s                 %12d%n")
        format.append("- Total          %13s                 %12d%n")

        val benchString = String.format(
            format.toString(),
            " ",
            "(ms)",
            SimpleTimer.toString(totalExecutionTime),
            totalExecutionTime,
            SimpleTimer.toString(totalNetworkTime),
            totalNetworkTime,
            SimpleTimer.toString(totalEffectiveTime),
            totalEffectiveTime,
            SimpleTimer.toString(totalWaitingTime),
            totalWaitingTime,
            SimpleTimer.toString(totalCompTime),
            totalCompTime,
            " ",
            "(bytes)",
            hSentBytes,
            sentBytes,
            hReceivedBytes,
            receivedBytes,
            hTotalBytes,
            totalBytes
        )

        mixnet.log.plainInfo(benchString)

        // If there is a Fiat-Shamir proof, then we print the size.
        val nizkpBytes = mixnet.nizkpBytes
        if (nizkpBytes > 0) {
            val hNizkpBytes = ExtIO.bytesToHuman(nizkpBytes)
            val nizkpString = String.format(
                "Proof size:      %13s                 %12d%n",
                hNizkpBytes, nizkpBytes
            )

            mixnet.log.plainInfo(nizkpString)
        }
    }
}