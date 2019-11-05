#!/bin/bash

set -e

BASEDIR=$(dirname $0)
START_DIR=$(pwd)

if [ "$SKIP_NETWORK" != true ]; then
    echo "#################"
    echo "# SETUP NETWORK #"
    echo "#################"
    bash $BASEDIR/../../network/tf_start.sh
fi

echo "#############"
echo "# SETUP CLI #"
echo "#############"
cd $BASEDIR/../../cli
npm install
npm run build
cd $START_DIR

echo "#########################"
echo "# CREATE PURCHASE ORDER #"
echo "#########################"
PO_CLI_PORT=6000 expect $BASEDIR/create-purchase-order.exp

echo ""
echo "##########################"
echo "# APPROVE PURCHASE ORDER #"
echo "##########################"
PO_CLI_PORT=6001 expect $BASEDIR/approve-purchase-order.exp

echo ""
echo "##########################"
echo "# CREATE FINANCE REQUEST #"
echo "##########################"
PO_CLI_PORT=6001 expect $BASEDIR/create-finance-request.exp

echo ""
echo "##########################"
echo "# REJECT FINANCE REQUEST #"
echo "##########################"
SP_CLI_PORT=7002 expect $BASEDIR/reject-finance-request.exp

echo ""
echo "##############"
echo "# BAD VERIFY #"
echo "##############"
SP_CLI_PORT=7001 expect $BASEDIR/bad-verify.exp

echo ""
echo "###########################"
echo "# APPROVE FINANCE REQUEST #"
echo "###########################"
SP_CLI_PORT=7003 expect $BASEDIR/approve-finance-request.exp

echo ""
echo "##########################"
echo "# ACCEPT FINANCE REQUEST #"
echo "##########################"
PO_CLI_PORT=6001 expect $BASEDIR/accept-finance-request.exp

if [ "$SKIP_NETWORK" != true ]; then
    echo "###################"
    echo "# CLEANUP NETWORK #"
    echo "###################"
    bash $BASEDIR/../../network/tf_teardown.sh
fi
