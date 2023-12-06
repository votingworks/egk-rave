#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Shuffling encrypted ballots..."

EG_WORKSPACE="${WORKSPACE_DIR}/eg"
CONSTANTS="${EG_WORKSPACE}/constants.json"
ELECTION_PARAMS="${EG_WORKSPACE}/election_initialized.json"

VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

CLASSPATH="build/libs/rave-mixnet-all.jar"

# shuffle once
rave_print "... now shuffling once ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.rave.RunMixnet \
    -in $input/input-ciphertexts.bt \
    -privInfo $input/privInfo.xml \
    -protInfo $input/protInfo.xml \
    -sessionId mix1

# shuffle twice
rave_print "... now shuffling twice ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.rave.RunMixnet \
    -in $working/nizkp/mix1/ShuffledCiphertexts.bt \
    -privInfo $input/privInfo.xml \
    -protInfo $input/protInfo.xml \
    -sessionId mix2

rave_print "[DONE] Shuffled encrypted ballots are in ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.json"
