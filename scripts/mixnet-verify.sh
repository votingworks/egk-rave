#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Verifying shuffled ballots..."

EG_BB="${WORKSPACE_DIR}/bb/eg"
VF_BB="${WORKSPACE_DIR}/bb/vf"

CLASSPATH="build/libs/egk-rave-all.jar"

rave_print "... verify mix1 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetVerifier \
    -protInfo ${VF_BB}/protocolInfo.xml \
    -shuffle ${VF_BB}/mix1 \
    --sessionId mix1 \
    -width 34

rave_print "... verify mix2 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetVerifier \
    -protInfo ${VF_BB}/protocolInfo.xml \
    -shuffle ${VF_BB}/mix2 \
    --sessionId mix2 \
    -width 34

rave_print "[DONE] Verifying shuffled ballots"
