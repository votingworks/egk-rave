# Workflow and Command Line Programs

last update 12/09/2023

<!-- TOC -->
* [Workflow and Command Line Programs](#workflow-and-command-line-programs)
  * [Rave Workflow](#rave-workflow)
    * [Generate test input ballots](#generate-test-input-ballots)
    * [Encrypt ballots to simulate receiving encrypted ballots from BB](#encrypt-ballots-to-simulate-receiving-encrypted-ballots-from-bb)
    * [Extract encrypted ballots for mixnet input](#extract-encrypted-ballots-for-mixnet-input)
    * [Reencrypt ballots to simulate paper ballot scanning](#reencrypt-ballots-to-simulate-paper-ballot-scanning)
    * [Run MixnetBlindTrustPep to compare paper ballots with mixnet](#run-mixnetblindtrustpep-to-compare-paper-ballots-with-mixnet)
    * [Run Verify Pep](#run-verify-pep)
<!-- TOC -->

## Rave Workflow

### Generate test input ballots

````
/usr/lib/jvm/jdk-19/bin/java \
  -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunCreateInputBallots \
    -manifest egklib/src/commonTest/data/rave/working/eg/manifest.json \
    -out testOut/rave/inputBallots  \
    -n 33 \
    -json
````

### Encrypt ballots to simulate receiving encrypted ballots from BB

````
/usr/lib/jvm/jdk-19/bin/java \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunBatchEncryption \
    -in egklib/src/commonTest/data/rave/working/eg/keyceremony \
    -ballots testOut/rave/inputBallots \
    -encryptDir testOut/rave/EB \
    -device device42 \
    -nthreads 33 
````

### Extract encrypted ballots for mixnet input

````
Usage: RunMakeMixnetInput options_list
Options: 
    --encryptedBallotsDir, -eballots -> Directory containing input encrypted ballots (EB) (always required) { String }
    --outputFile, -out -> Write to this filename (always required) { String }
    --help, -h -> Usage info 
````

Example:

````
/usr/lib/jvm/jdk-19/bin/java \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunMakeMixnetInput \
    -eballots testOut/rave/EB \
    -out testOut/rave/vg/input-ciphertexts.json
````


### Reencrypt ballots to simulate paper ballot scanning

````
/usr/lib/jvm/jdk-19/bin/java \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunBatchEncryption \
    -in egklib/src/commonTest/data/rave/working/eg/keyceremony \
    -ballots testOut/rave/inputBallots  \
    -encryptDir testOut/rave/PB  \
    -device scanPB \
    -nthreads 33 \
    --anonymize
````


### Run MixnetBlindTrustPep to compare paper ballots with mixnet

```` 
Usage: RunMixnetBlindTrustPep options_list
Options: 
    --inputDir, -in -> Top directory of the input election record (always required) { String }
    --mixnetFile, -file with mixnet output -> Json file containing mixnet ballot output (always required) { String }
    --trusteeDir, -trustees -> Directory to read private trustees (always required) { String }
    --outputDir, -out -> Directory to write output election record (always required) { String }
    --missing, -missing -> missing guardians' xcoord, comma separated, eg '2,4' { String }
    --nthreads, -nthreads [11] -> Number of parallel threads to use { Int }
    --help, -h -> Usage info 
````

Example:

````
/usr/lib/jvm/jdk-19/bin/java \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunMixnetBlindTrustPep \
    -in egklib/src/commonTest/data/rave/working/eg/keyceremony \
    -eballots testOut/rave/PB \
    --mixnetFile egklib/src/commonTest/data/rave/working/vf/after-mix-2-ciphertexts.json \
    -trustees egklib/src/commonTest/data/rave/working/eg/trustees \
    -out testOut/rave/pep \
    -nthreads 33 
````

### Run Verify Pep

```` 
Usage: RunVerifyPep options_list
Options: 
    --inputDir, -in -> Directory containing input election record (always required) { String }
    --pepBallotDir, -pep -> Directory containing PEP output (always required) { String }
    --nthreads, -nthreads [11] -> Number of parallel threads to use { Int }
    --help, -h -> Usage info 
````

Example:

````
/usr/lib/jvm/jdk-19/bin/java \
  -classpath egkliball/build/libs/egklib-all.jar \
  electionguard.cli.RunVerifyPep \
    --inputDir egklib/src/commonTest/data/rave/working/eg/keyceremony \
    --pepBallotDir testOut/rave/pep
````
