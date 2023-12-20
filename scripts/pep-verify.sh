#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Verify the PEP output."

CLASSPATH="build/libs/egk-rave-all.jar"

java \
  -classpath ${CLASSPATH} \
  org.cryptobiotic.pep.RunVerifyPep \
    -in ${WORKSPACE_DIR}/eg \
    -pep ${WORKSPACE_DIR}/bb/pep/ \

rave_print "[DONE] PEP verify."
