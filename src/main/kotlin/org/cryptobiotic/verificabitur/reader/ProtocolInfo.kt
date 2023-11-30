package org.cryptobiotic.verificabitur.reader

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.serialization.*

import java.io.File

fun readProtocolInfo(filename : String ) : ProtocolInfo {
    println("readProtocolInfo filename = ${filename}")

    //gulp the entire file to a string
    val file = File(filename)
    val text = file.readText(Charsets.UTF_8)

    val serializer = serializer<ProtocolInfo>() // use the default serializer

    // Create the configuration for (de)serialization
    val xml = XML { indent = 2 }

    val protInfo : ProtocolInfo = xml.decodeFromString(serializer, text)
    println("$protInfo")
    return protInfo
}

enum class ProofOfCorrectness { interactive, noninteractive}

// TODO reflect all of the possible parameters to vmni (appendix H, user manual). Probably get the defaults?
@Serializable
@XmlSerialName(value = "protocol")
data class ProtocolInfo(
    @XmlElement val version: String,
    @XmlElement val sid: String,
    @XmlElement val name: String,
    @XmlElement val descr: String,
    @XmlElement val nopart: Int,
    @XmlElement val thres: Int,
    @XmlElement val statdist: Int,
    @XmlElement val bullboard: String,
    @XmlElement val pgroup: String,
    @XmlElement val keywidth: Int,
    @XmlElement val vbitlen: Int,
    @XmlElement val vbitlenro: Int,
    @XmlElement val ebitlen: Int,
    @XmlElement val ebitlenro: Int,
    @XmlElement val prg: String,
    @XmlElement val rohash: String,
    @XmlElement @XmlSerialName(value = "corr") val corr: ProofOfCorrectness,
    @XmlElement val width: Int,
    @XmlElement val maxciph: Int,
    val parties: List<Party>,
) {
    override fun toString(): String {
        return buildString {
            appendLine("ProtocolInfo(version='$version'")
            appendLine("  sid='$sid', name='$name', descr='$descr', bullboard='$bullboard'")
            appendLine("  nparties=$nopart, threshold=$thres, statdist=$statdist, keywidth=$keywidth, vbitlen=$vbitlen")
            appendLine("  vbitlenro=$vbitlenro, ebitlen=$ebitlen, ebitlenro=$ebitlenro, prg='$prg', rohash='$rohash', proofOfCorrectness=$corr width=$width, maxciph=$maxciph")
            appendLine("  pgroup='$pgroup'")
            parties.forEach { append(it) }
        }
    }
}

@Serializable
@XmlSerialName(value = "party")
data class Party(
    @XmlElement val name: String,
    @XmlElement val srtbyrole: String,
    @XmlElement val descr: String?,
    @XmlElement val pkey: String,
    @XmlElement val http: String,
    @XmlElement val hint: String,
) {
    override fun toString(): String {
        return buildString {
            appendLine(" Party(name='$name', srtbyrole='$srtbyrole', descr=$descr, http='$http', hint='$hint'")
            appendLine("  pkey='$pkey'")
        }
    }
}

/*
<protocol>

   <!-- Version of Verificatum Software for which this info is intended. -->
   <version>3.1.0</version>

   <!-- Session identifier of this protocol execution. This must be
        globally unique and satisfy the regular expression [A-Za-z][A-Za-z0-
        9]{1,1023}. -->
   <sid>MyDemo</sid>

   <!-- Name of this protocol execution. This is a short descriptive name
        that is NOT necessarily unique, but satisfies the regular
        expression [A-Za-z][A-Za-z0-9_ ]{1,255}. -->
   <name>Swedish Election</name>

   <!-- Description of this protocol execution. This is merely a longer
        description than the name of the protocol execution. It must
        satisfy the regular expression |[A-Za-z][A-Za-z0-9:;?!.()\[\] ]
        {0,4000}. -->
   <descr></descr>

   <!-- Number of parties taking part in the protocol execution. This must
        be a positive integer that is at most 25. -->
   <nopart>3</nopart>

   <!-- Statistical distance from uniform of objects sampled in protocols
        or in proofs of security. This must be a non-negative integer at
        most 256. -->
   <statdist>100</statdist>

   <!-- Name of bulletin board implementation used, i.e., a subclass of com.
        verificatum.protocol.com.BullBoardBasic. WARNING! This field is not
        validated syntactically. -->
   <bullboard>com.verificatum.protocol.com.BullBoardBasicHTTPW</bullboard>

   <!-- Threshold number of parties needed to violate the privacy of the
        protocol, i.e., this is the number of parties needed to decrypt.
        This must be positive, but at most equal to the number of parties.
        -->
   <thres>2</thres>

   <!-- Group over which the protocol is executed. An instance of a
        subclass of com.verificatum.arithm.PGroup. -->
   <pgroup>ECqPGroup(P-224)::00000000020100000020636f6d2e766572696669636174756d2e61726974686d2e4543715047726f75700100000005502d323234</pgroup>

   <!-- Width of El Gamal keys. If equal to one the standard El Gamal
        cryptosystem is used, but if it is greater than one, then the
        natural generalization over a product group of the given width is
        used. This corresponds to letting each party holding multiple
        standard public keys. -->
   <keywidth>1</keywidth>

   <!-- Bit length of challenges in interactive proofs. -->
   <vbitlen>128</vbitlen>

   <!-- Bit length of challenges in non-interactive random-oracle proofs.
        -->
   <vbitlenro>256</vbitlenro>

   <!-- Bit length of each component in random vectors used for batching.
        -->
   <ebitlen>128</ebitlen>

   <!-- Bit length of each component in random vectors used for batching in
        non-interactive random-oracle proofs. -->
   <ebitlenro>256</ebitlenro>

   <!-- Pseudo random generator used to derive random vectors for
        batchingfrom jointly generated seeds. This can be "SHA-256", "SHA-
        384", or "SHA-512", in which case com.verificatum.crypto.
        PRGHeuristic is instantiated based on this hashfunction, or it can
        be an instance of com.verificatum.crypto.PRG. WARNING! This field
        is not validated syntactically. -->
   <prg>SHA-256</prg>

   <!-- Hashfunction used to implement random oracles. It can be one of the
        strings "SHA-256", "SHA-384", or "SHA-512", in which case com.
        verificatum.crypto.HashfunctionHeuristic is instantiated, or an
        instance of com.verificatum.crypto.Hashfunction. Random oracles
        with various output lengths are then implemented, using the given
        hashfunction, in com.verificatum.crypto.RandomOracle.
        WARNING! Do not change the default unless you know exactly what you
        are doing. This field is not validated syntactically. -->
   <rohash>SHA-256</rohash>

   <!-- Determines if the proofs of correctness of an execution are
        interactive or non-interactive. Legal valus are "interactive" or
        "noninteractive". -->
   <corr>noninteractive</corr>

   <!-- Default width of ciphertexts processed by the mix-net. A different
        width can still be forced for a given session by using the "-width"
        option. -->
   <width>1</width>

   <!-- Maximal number of ciphertexts for which precomputation is
        performed. Pre-computation can still be forced for a different
        number of ciphertexts for a given session using the "-maxciph"
        option during pre-computation. -->
   <maxciph>0</maxciph>

   <party>

      <!-- Name of party. This must satisfy the regular expression [A-Za-z][A-
           Za-z0-9_ ]{1,255}. -->
      <name>Party01</name>

      <!-- Sorting attribute used to sort parties with respect to their roles
           in the protocol. This is used to assign roles in protocols where
           different parties play different roles. -->
      <srtbyrole>anyrole</srtbyrole>

      <!-- Description of this party. This is merely a longer description
           than the name of the party. It must satisfy the regular expression
           |[A-Za-z][A-Za-z0-9:;?!.()\[\] ]{0,4000}. -->
      <descr></descr>

      <!-- Public signature key (instance of subclasses of com.verificatum.
           crypto.SignaturePKey). WARNING! This field is not validated
           syntactically. -->
      <pkey>SignaturePKeyHeuristic(RSA, bitlength=2048)::0000000002010000002d636f6d2e766572696669636174756d2e63727970746f2e5369676e6174757265504b65794865757269737469630000000002010000012630820122300d06092a864886f70d01010105000382010f003082010a0282010100a8656c8d3bdac6dddd33c590ac74ceca14567a9c0f7826779a3aa1b610c2c3ed92a1b6402c8cd39324101923569f7a660e7c1e0ce96c24c1e2a8b171529dbab760aba41818678c8b20352984bac167a7e46ae198443e8e30c0fbeb2e92e48ac86725df3cc3d9e6007ffd644c8d5d1325b06f7cb7e6bf39105d5f215c59935301eec6f97f4c9c62e9f80b8df9544528800f7c0ef866accd6acb159821e942579f05f7b449f7fe56c475c0e5a3e7a482b2d05c3f82474bb2e4a0d67e889b245e9493e11a805307565326f5130dff8a91be7a6cb987f59b11cf9e87af8b106371a054394ea8f78a0108a208a4926179a039dff4396a4f8539a730f2a9ff221befdf0203010001010000000400000800</pkey>

      <!-- URL to the HTTP server of this party. -->
      <http>http://localhost:8041</http>

      <!-- Socket address given as <hostname>:<port> or <ip address>:<port>
           to our hint server. A hint server is a simple UDP server that
           reduces latency and traffic on the HTTP servers. -->
      <hint>localhost:4041</hint>

   </party>

   <party>

      <!-- Name of party. This must satisfy the regular expression [A-Za-z][A-
           Za-z0-9_ ]{1,255}. -->
      <name>Party02</name>

      <!-- Sorting attribute used to sort parties with respect to their roles
           in the protocol. This is used to assign roles in protocols where
           different parties play different roles. -->
      <srtbyrole>anyrole</srtbyrole>

      <!-- Description of this party. This is merely a longer description
           than the name of the party. It must satisfy the regular expression
           |[A-Za-z][A-Za-z0-9:;?!.()\[\] ]{0,4000}. -->
      <descr></descr>

      <!-- Public signature key (instance of subclasses of com.verificatum.
           crypto.SignaturePKey). WARNING! This field is not validated
           syntactically. -->
      <pkey>SignaturePKeyHeuristic(RSA, bitlength=2048)::0000000002010000002d636f6d2e766572696669636174756d2e63727970746f2e5369676e6174757265504b65794865757269737469630000000002010000012630820122300d06092a864886f70d01010105000382010f003082010a0282010100c4d43777a4e70576eb10053688e06ddaa04b2d7c1a3f5e1f48064619b20d1c8b9b769d59a5146320dfce8eabd9a86056b4e890a601b07f8e2bb9fe2dc6d9945ef831fbc3ec7ace78e3023fb0e68da1edf801feed9221f577430ae40306bad6b8c0e7df62e914685584a9605f209701b5614bfc2e4a0b5a2b942a9edf2a65cda878dba842c0fe81384846c2b49144893eeaf34b713b0c4294c240cf380098d9ba7807cc8fa386407e8a2a9e676bcd08f5d00b2f612181243f87820175fea48cb16c0931ff4a0be3344397e988678580fb7e37051565b87767711cf2e19b0316eb45d7215161ea76bb3b95791353949ea9a7efd38affb77b6cc4edafeb18680b9f0203010001010000000400000800</pkey>

      <!-- URL to the HTTP server of this party. -->
      <http>http://localhost:8042</http>

      <!-- Socket address given as <hostname>:<port> or <ip address>:<port>
           to our hint server. A hint server is a simple UDP server that
           reduces latency and traffic on the HTTP servers. -->
      <hint>localhost:4042</hint>

   </party>

   <party>

      <!-- Name of party. This must satisfy the regular expression [A-Za-z][A-
           Za-z0-9_ ]{1,255}. -->
      <name>Party03</name>

      <!-- Sorting attribute used to sort parties with respect to their roles
           in the protocol. This is used to assign roles in protocols where
           different parties play different roles. -->
      <srtbyrole>anyrole</srtbyrole>

      <!-- Description of this party. This is merely a longer description
           than the name of the party. It must satisfy the regular expression
           |[A-Za-z][A-Za-z0-9:;?!.()\[\] ]{0,4000}. -->
      <descr></descr>

      <!-- Public signature key (instance of subclasses of com.verificatum.
           crypto.SignaturePKey). WARNING! This field is not validated
           syntactically. -->
      <pkey>SignaturePKeyHeuristic(RSA, bitlength=2048)::0000000002010000002d636f6d2e766572696669636174756d2e63727970746f2e5369676e6174757265504b65794865757269737469630000000002010000012630820122300d06092a864886f70d01010105000382010f003082010a0282010100a757e07199562565b4ca81476b3668feb0c9b39ea3948f81b9d682636b61a63a0c0265b0246eb3fe726f93d0a99a4e29de9b69e5b419d37a36f4bdd3e319d5e1b93cd7f258655acd61f001297e106990b9a734a008fa4287d76a8bd0a72a60e8774e5930187ff66778a27f99b9949c912b5e74d2bf9824e6275e9cefe4d6e0877ca1028d1c591108f5ab053d4f614db9e806ec97bed7df3e6bb5c45b825c6f7f423fe6245569d1ce21675aa88cccf6a6086ef1195fc4e6ff3286eb31cb16dc971ce9159de1fc56a211ddd57168212e702a036f15def3ba9b2bd055023b6ff050b68a4df67607c3bc2051321ea9f37b2b7fcd6809f2b844cba31e27360af916bd0203010001010000000400000800</pkey>

      <!-- URL to the HTTP server of this party. -->
      <http>http://localhost:8043</http>

      <!-- Socket address given as <hostname>:<port> or <ip address>:<port>
           to our hint server. A hint server is a simple UDP server that
           reduces latency and traffic on the HTTP servers. -->
      <hint>localhost:4043</hint>

   </party>

</protocol>
 */


