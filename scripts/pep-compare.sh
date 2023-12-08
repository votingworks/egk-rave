#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1
VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Reencrypt ballots to simulate paper ballot scanning"

EGK_CLASSPATH="/home/stormy/dev/github/electionguard-kotlin-multiplatform/egkliball/build/libs/egklib-all.jar"

java \
  -classpath ${EGK_CLASSPATH} \
  electionguard.cli.RunBatchEncryption \
    -in ${WORKSPACE_DIR}/eg \
    -ballots ${WORKSPACE_DIR}/eg/inputBallots  \
    -eballots ${WORKSPACE_DIR}/bb/encryptedPaperBallots  \
    -device scanPaperBallots \
    --anonymize

rave_print "[DONE] Reencrypting ballots."

rave_print "Checking mixnet output against the reencrypted ballots with PEP algorithm"

CLASSPATH="build/libs/rave-mixnet-all.jar"

java \
  -classpath ${CLASSPATH} \
  org.cryptobiotic.mixnet.RunMixnetBlindTrustPep \
    -in ${WORKSPACE_DIR}/eg \
    -eballots ${WORKSPACE_DIR}/bb/encryptedPaperBallots  \
    -mixballots ${WORKSPACE_DIR}/bb/vf/mix2/ShuffledCiphertexts.bt \
    -trustees ${WORKSPACE_DIR}/eg/trustees \
    -out ${WORKSPACE_DIR}/bb/pep


rave_print "[DONE] Checking mixnet output."
