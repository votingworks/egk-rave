#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Verifying shuffled ballots..."

EG_WORKSPACE="${WORKSPACE_DIR}/eg"
CONSTANTS="${EG_WORKSPACE}/constants.json"
ELECTION_PARAMS="${EG_WORKSPACE}/election_initialized.json"

VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

CLASSPATH="build/libs/rave-mixnet-all.jar"

# shuffle once
rave_print "... verify mix1 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.rave.RunVerifier \
    -nizkp $input/mix1 \
    -protInfo $input/protInfo.xml \
    -sessionId mix1

# shuffle twice
rave_print "... verify mix2 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.rave.RunVerifier \
    -nizkp $input/mix2 \
    -protInfo $input/protInfo.xml \
    -sessionId mix2

rave_print "[DONE] Verifying shuffled ballots"
