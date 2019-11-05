#!/bin/bash

helpFunction()
{
   echo ""
   echo "Usage:"
   echo -e "\t-l language"
   echo -e "\t-p endorsment policy"
   echo -e "\t-d chaincode folder"
   echo -e "\t-n chaincode name"
   echo -e "\t-t instantiate/upgrade"
   echo -e "\t-c channel name"
   echo -e "\t-v version"
   exit 1 # Exit script after printing help
}

while getopts "l:p:d:n:t:v:c:" opt
do
   case "$opt" in
      l ) CHAINCODE_LANGUAGE="$OPTARG" ;;
      p ) POLICY="$OPTARG" ;;
      d ) CHAINCODE_FOLDER="$OPTARG" ;;
      n ) CHAINCODE_NAME="$OPTARG" ;;
      t ) INSTANTIATE_UPGRADE="$OPTARG" ;;
      v ) CHAINCODE_VERSION="$OPTARG" ;;
      c ) CHANNEL_NAME="$OPTARG" ;;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done

set -e

BASEDIR=$(dirname "$0")
CUR_DIR=$(pwd)

source $BASEDIR/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")

if [ -z "$CHANNEL_NAME" ]; then
    read -p "Enter name of channel to install on: " CHANNEL_NAME
fi

if [ -z "$CHAINCODE_FOLDER" ]; then
    read -p "Enter location of chaincode folder: (full path) " CHAINCODE_FOLDER
fi

CHAINCODE_BASE=$(basename "$CHAINCODE_FOLDER")

if [ -z "$CHAINCODE_NAME" ]; then
    read -p "Enter name for your chaincode: " CHAINCODE_NAME
fi

if [ -z "$POLICY" ]; then
    read -p "Enter endorsement policy: " POLICY
fi

if [ -z "$CHAINCODE_LANGUAGE" ]; then
    read -p "Enter the language of your chaincode: " CHAINCODE_LANGUAGE
fi

if [ -z "$INSTANTIATE_UPGRADE" ]; then
    read -p "Enter whether you wish to instantiate or upgrade: " INSTANTIATE_UPGRADE
fi

if [ $INSTANTIATE_UPGRADE == "upgrade" ]; then
    if [ -z "$CHAINCODE_VERSION" ]; then
        read -p "Enter the version of your chaincode: " CHAINCODE_VERSION
    fi
else
    CHAINCODE_VERSION=0
fi

echo "INSTALLING AND INSTANTIATING CHAINCODE: "
echo "CHANNEL: $CHANNEL_NAME"
echo "NAME: $CHAINCODE_NAME"
echo "FOLDER: $CHAINCODE_FOLDER"
echo "LANGUAGE: $CHAINCODE_LANGUAGE"
echo "POLICY: $POLICY"
echo "VESION: $CHAINCODE_VERSION"

echo "#################"
echo "# TAR CHAINCODE #"
echo "#################"
cd $CHAINCODE_FOLDER/..
tar -chf $CHAINCODE_NAME.tar $CHAINCODE_BASE
CHAINCODE_TAR=$(pwd)/$CHAINCODE_NAME.tar
cd $BASEDIR

echo "###############"
echo "# COPY TO CLI #"
echo "###############"
docker exec digi-bank-po_cli mkdir -p "/etc/hyperledger/contract"
docker cp "$CHAINCODE_TAR" "digi-bank-po_cli:/etc/hyperledger/contract"
docker exec digi-bank-po_cli bash -c "cd /etc/hyperledger/contract; tar -xhf $CHAINCODE_NAME.tar"

docker exec magneto-corp-po_cli mkdir -p "/etc/hyperledger/contract"
docker cp "$CHAINCODE_TAR" "magneto-corp-po_cli:/etc/hyperledger/contract"
docker exec magneto-corp-po_cli bash -c  "cd /etc/hyperledger/contract; tar -xhf $CHAINCODE_NAME.tar"

docker exec hedgematic-po_cli mkdir -p "/etc/hyperledger/contract"
docker cp "$CHAINCODE_TAR" "hedgematic-po_cli:/etc/hyperledger/contract"
docker exec hedgematic-po_cli bash -c  "cd /etc/hyperledger/contract; tar -xhf $CHAINCODE_NAME.tar"

echo "################"
echo "# CLEANUP TARS #"
echo "################"
rm -rf $CHAINCODE_TAR

echo "#####################"
echo "# CHAINCODE INSTALL #"
echo "#####################"

echo "####################################"
echo "# CHAINCODE INSTALL DIGI BANK #"
echo "####################################"
docker exec digi-bank-po_cli peer chaincode install -l $CHAINCODE_LANGUAGE -n $CHAINCODE_NAME -v $CHAINCODE_VERSION -p "/etc/hyperledger/contract/$CHAINCODE_BASE"

echo "######################################"
echo "# CHAINCODE INSTALL MAGNETO CORP #"
echo "######################################"
docker exec magneto-corp-po_cli peer chaincode install -l $CHAINCODE_LANGUAGE -n $CHAINCODE_NAME -v $CHAINCODE_VERSION -p "/etc/hyperledger/contract/$CHAINCODE_BASE"

echo "########################################"
echo "# CHAINCODE INSTALL HEDGEMATIC BANKING #"
echo "########################################"
docker exec hedgematic-po_cli peer chaincode install -l $CHAINCODE_LANGUAGE -n $CHAINCODE_NAME -v $CHAINCODE_VERSION -p "/etc/hyperledger/contract/$CHAINCODE_BASE"

echo "#########################"
echo "# CHAINCODE INSTANTIATE #"
echo "#########################"
docker exec digi-bank-po_cli peer chaincode $INSTANTIATE_UPGRADE -o orderer.example.com:7050 \
-l $CHAINCODE_LANGUAGE -C $CHANNEL_NAME -n $CHAINCODE_NAME -v $CHAINCODE_VERSION \
--tls true \
--cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem \
-c '{"Args":[]}' -P "$POLICY" --collections-config /etc/hyperledger/collections/collections_config.json

wait_until 'docker ps -a | grep dev-peer0.digi-bank-po > /dev/null' 3 10

CHAINCODE_QUERY="peer chaincode query -o orderer.example.com:7050 \
-C $CHANNEL_NAME -n $CHAINCODE_NAME \
--tls true \
--cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem \
-c '{\"Args\":[\"org.hyperledger.fabric:GetMetadata\"]}' > /dev/null"

wait_until "docker exec digi-bank-po_cli $CHAINCODE_QUERY" 3 10
wait_until "docker exec magneto-corp-po_cli $CHAINCODE_QUERY" 3 10
wait_until "docker exec hedgematic-po_cli $CHAINCODE_QUERY" 3 10
