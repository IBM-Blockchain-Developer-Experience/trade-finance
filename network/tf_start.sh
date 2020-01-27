#!/bin/bash

set -e

BASEDIR=$(dirname "$0")

source $BASEDIR/network/scripts/utils.sh
source $BASEDIR/network/scripts/spinner.sh

BASEDIR=$(get_full_path "$BASEDIR")
NETWORK=$BASEDIR/network
CLI_DIR=$BASEDIR/cli_tools
MODULEDIR=$(get_full_path "$BASEDIR/..")

if [ ! -d "$CLI_DIR/dist" ] || [ -z $(docker images -q tradefinance/porest) ] || [ -z $(docker images -q tradefinance/sprest) ]; then
    echo "##############################"
    echo "# SETUP NOT RUN. RUNNING NOW #"
    echo "##############################"

    $BASEDIR/tf_setup.sh
fi

mkdir -p "$BASEDIR/tmp"

echo "#################"
echo "# SETUP NETWORK #"
echo "#################"
bash $NETWORK/network_start.sh


echo "###################"
echo "# START CHAINCODE #"
echo "###################"

set +e

CHAINCODE_FOLDER=$MODULEDIR/contracts
echo "Instantiating Purchase Order Contract..."
bash $NETWORK/scripts/install_and_instantiate.sh -t instantiate -l java -d $CHAINCODE_FOLDER/purchase_order_contract -p "OR('DigiBankPOMSP.member', 'MagnetoCorpPOMSP.member', 'HedgematicPOMSP.member')" -n purchasecontract -c tradenetpurchase > $BASEDIR/tmp/purchasecontract_startup.log 2>&1 &
PURCHASE_INSTANTIATE_ID=$!

start_spinner  "Instantiating Finance Request Contract..."
bash $NETWORK/scripts/install_and_instantiate.sh -t instantiate -l java -d $CHAINCODE_FOLDER/finance_contract -p "OR('DigiBankPOMSP.member', 'MagnetoCorpPOMSP.member', 'HedgematicPOMSP.member')" -n financecontract -c tradenetfinance > $BASEDIR/tmp/financecontract_startup.log 2>&1 &
FINANCE_INSTANTIATE_ID=$!

set -e

if wait $PURCHASE_INSTANTIATE_ID && wait $FINANCE_INSTANTIATE_ID; then
    PURCHASE_INSTANTIATE_EXIT=$?
    FINANCE_INSTANTIATE_EXIT=$?
    stop_spinner 0
    echo "Purchase Order contract instantiated"
    echo "Finance Request contract instantiated"
else
    stop_spinner 1
    echo "Failed to instantiate chaincode. Processes exited with:"
    echo "Purchase contract: $PURCHASE_INSTANTIATE_EXIT"
    echo "Finance contract: $FINANCE_INSTANTIATE_EXIT"
    echo "Check the logs for more details: $BASEDIR/tmp"

    if [ "$PURCHASE_INSTANTIATE_EXIT" != 0 ]
    then
        cat $BASEDIR/tmp/purchasecontract_startup.log
    fi
    if [ "$FINANCE_INSTANTIATE_EXIT" != 0 ]
    then
        cat $BASEDIR/tmp/financecontract_startup.log
    fi

    exit 1
fi

echo "########################################"
echo "# IMPORTING ADMINS AND ENROLLING USERS #"
echo "########################################"

WALLET_PATH=$BASEDIR/wallets

for IDENTITY_FOLDER in `find $NETWORK/tradenet_fabric/identities/ -type d -maxdepth 1 -mindepth 1`; do
    ORG=$(basename $IDENTITY_FOLDER)
    ORG_WALLET=$BASEDIR/wallets/$ORG

    mkdir -p $ORG_WALLET

    node $CLI_DIR/dist/index.js import -w $ORG_WALLET -m "${ORG}MSP" -n admin -c $IDENTITY_FOLDER/admin/cert.pem -k $IDENTITY_FOLDER/admin/key.pem

    CONNECTION_PROFILE=$NETWORK/tradenet_fabric/${ORG}_local_connection.json
    ATTRIBUTES_JSON=$BASEDIR/fixtures/attributes/${ORG: -2}.json
    GLN_JSON=$BASEDIR/fixtures/attributes/${ORG}.json
    MERGED_ATTRIBUTES=$(jq '.[.| length] |= . + input' $ATTRIBUTES_JSON $GLN_JSON)

    TMP_JSON=$BASEDIR/tmp/${ORG}.json

    echo $MERGED_ATTRIBUTES > $TMP_JSON

    node $CLI_DIR/dist/index.js enroll -w $ORG_WALLET -o $ORG -c $CONNECTION_PROFILE -n services -N admin -a $TMP_JSON
done

rm -rf $BASEDIR/tmp

echo "#############################"
echo "# GENERATE VS CODE NODE FILE#"
echo "#############################"
if ! hash blockchain-node-generator 2>/dev/null;
then
    echo "'blockchain-node-generator' was not found in PATH"
else
    blockchain-node-generator --networkName node_default || echo "Unable to create an env file. Check the README.md for more information"
fi

echo "##############"
echo "# SETUP APIS #"
echo "##############"
docker-compose -f $BASEDIR/apps/docker-compose/docker-compose.yaml -p node up -d
wait_until 'curl -s localhost:6000/api > /dev/null' 12 20
wait_until 'curl -s localhost:6001/api > /dev/null' 12 20
wait_until 'curl -s localhost:6002/api > /dev/null' 12 20
wait_until 'curl -s localhost:7000/api > /dev/null' 12 20
wait_until 'curl -s localhost:7001/api > /dev/null' 12 20
wait_until 'curl -s localhost:7002/api > /dev/null' 12 20
wait_until 'curl -s localhost:7003/api > /dev/null' 12 20
