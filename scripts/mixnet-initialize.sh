#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Initialize verificatum mixnet..."

EG_WORKSPACE="${WORKSPACE_DIR}/eg"
VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

CLASSPATH="build/libs/rave-mixnet-all.jar"
java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetConfig \
    -input ${EG_WORKSPACE} \
    -working ${VERIFICATUM_WORKSPACE}

rave_print "[DONE] Initialize verificatum mixnet in directory ${VERIFICATUM_WORKSPACE}"
