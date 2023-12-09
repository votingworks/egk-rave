# Rave Cryptography

_last update 12/08/2023_

This is the kotlin/java code for RAVE cryptography. It builds on top of 

* [ElectionGuard-Kotlin](https://github.com/votingworks/electionguard-kotlin-multiplatform)
* [Verificatum Mixnet](https://www.verificatum.org/)

See [RAVE Cryptography Implementation](https://github.com/votingworks/rave-cryptography-implementation) for more information.

## Sample Workflow for testing

`~/dev/github/rave-mixnet:$ ./scripts/runCompleteWorkflow.sh
`

Runs a complete test RAVE workflow and writes the output to the working directory. The components of this workflow are:

###  initialize-election.sh working

1. Deletes the working directory and starts fresh.
2. Uses _famous-names-election.json_ to create an electionguard _manifest.json_.
3. Create an electiongurad configuration file with default election parameters.
4. Runs the electionguard keyceremony to create private trustee keys and a public election key.

###  generate-and-encrypt-ballots.sh working nballots

1. Generates random plaintext ballots from the given manifest.
2. Encrypts those ballots with the public key.

###  tally-encrypted-ballots.sh working

1. Homomorphically accumulates encrypted ballots into an encrypted tally.

###  tally-decrypt.sh working

1. Uses trustee keys to decrypt the tally.

###  make-mixnet-input.sh working

1. Writes encrypted ballots to the format that the verificatum mixnet requires.

###  mixnet-initialize.sh working

1. Initializes the verificatum mixnet.

###  mixnet-shuffle.sh working

1. Shuffles the ballots using two shuffling phases, and writes out the shuffled ballots and their proof of shuffle.

###  copy-to-bb.sh working

1. Copies the needed information from the working directories to the public bulletin board.

###  mixnet-verify.sh working

1. Runs the verificatum verifier on the mixnet proofs.

###  pep-compare.sh working

1. Creates a separate encoding of the plaintext ballots to simulate the paper ballot encryption.
2. Uses the serial numbers to match the paper ballot encryptions to the mixnet shuffled ballots. Requires knowledge of the trustee keys.
3. Compares these with the PEP (Plaintext Equivalence Proof) algorithm.
4. Writes out the PEP proofs for each ballot.

###  pep-verify.sh working

1. Validates the PEP proofs.


## Bulletin Board file layout (strawman)

````

eg/
    constants.json
    election_config.json
    election_initialized.json
    encrypted_tally.json
    manifest.json
    tally.json

encryptedBallots/
    eballot-45874.json
    eballot-74766.json
    ...
    
encryptedPaperBallots/
    eballot-1.json
    eballot-10.json
    eballot-2.json
    ...    

pep/
    pepballot-1.json
    pepballot-10.json
    pepballot-2.json
    ...

vf/
    protocolInput.xml
    publicKey.bt
    mix1/
        Ciphertexts.bt
        FullPublicKey.bt
        ShuffledCiphertexts.bt
        proofs/
            PermutationCommittment01.bt
            PoSCommittment01.bt
            PosReply01.bt
    mix2/
        Ciphertexts.bt
        FullPublicKey.bt
        ShuffledCiphertexts.bt
        proofs/
            PermutationCommittment01.bt
            PoSCommittment01.bt
            PosReply01.bt`
````

Notes

1. encryptedPaperBallots/ and pep/ only exist if pep-compare.sh is run.
2. Note that when the "paper ballots" are encrypted, the ballot id is removed.


## Authors
- [John Caron](https://github.com/JohnLCaron) (Rave Cryptography, ElectionGuard Kotlin)
- [Dan S. Wallach](https://www.cs.rice.edu/~dwallach/) (ElectionGuard Kotlin)
- [Douglas Wikström](https://www.verificatum.org/) (Verificatum Mixnet)