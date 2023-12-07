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

# TODO
rave_print "Set public key"
# - Set an externally generated public key to be used during shuffling (without decrypting).
# The key must be given in the raw format for the group specified in the info file and with the proper key width.
# Consider using the vmnc command to convert public keys in other formats.
vmn -setpk -e ${VERIFICATUM_WORKSPACE}/privateInfo.xml ${VERIFICATUM_WORKSPACE}/protocolInfo.xml ${VERIFICATUM_WORKSPACE}/publicKey.bt


rave_print "[DONE] Initialize verificatum mixnet in directory ${VERIFICATUM_WORKSPACE}"
