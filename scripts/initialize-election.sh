#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1
VX_DEF="scripts/"$2

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

if [ -z "${VX_DEF}" ]; then
    rave_print "No Vx election definition provided."    
    exit 1
fi

rave_print "Initializing Election: reinitialize ${WORKSPACE_DIR} directory"

rm -rf ${WORKSPACE_DIR}/*

mkdir -p  ${WORKSPACE_DIR}/eg

rave_print "  build sample electionguard manifest from ${VX_DEF}"

node scripts/election-definition-convert-vx-to-eg.js ${VX_DEF} ${WORKSPACE_DIR}/eg/manifest.json

rave_print "  build sample electionguard configuration"

CLASSPATH="/home/stormy/dev/github/electionguard-kotlin-multiplatform/egkliball/build/libs/egklib-all.jar"

 java -classpath CLASSPATH electionguard.cli.RunCreateElectionConfig \
    -manifest ${WORKSPACE_DIR}/eg/manifest.json \
    -nguardians 3 \
    -quorum 3 \
    -out ${WORKSPACE_DIR}/eg \
    --baux0 device42

rave_print "   generate Keypair"

java -classpath CLASSPATH electionguard.cli.RunTrustedKeyCeremony \
    -in ${WORKSPACE_DIR}/eg \
    -trustees ${WORKSPACE_DIR}/eg/trustees \
    -out ${WORKSPACE_DIR}/eg

rave_print "[DONE] Generating election initialization in ${WORKSPACE_DIR}/eg"
