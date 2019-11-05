#!/bin/bash

helpFunction()
{
   echo ""
   echo "Usage:"
   echo -e "\t-p profile name"
   echo -e "\t-n channel name"
   exit 1 # Exit script after printing help
}

while getopts "p:n:g:" opt
do
   case "$opt" in
      p ) PROFILE="$OPTARG" ;;
      n ) CHANNEL_NAME="$OPTARG" ;;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done

set -e

BASEDIR=$(dirname "$0")
CUR_DIR=$(pwd)

source $BASEDIR/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")

if [ -z "$PROFILE" ]; then
    read -p "Enter profile to use: " PROFILE
fi

if [ -z "$CHANNEL_NAME" ]; then
    read -p "Enter name of channel: " CHANNEL_NAME
fi

echo "JOINING CHANNEL: "
echo "PROFILE: $PROFILE"
echo "CHANNEL ID: $CHANNEL_NAME"

docker exec digi-bank-po_cli peer channel create -o orderer.example.com:7050 -c $CHANNEL_NAME -f /etc/hyperledger/configtx/$PROFILE.tx \
    --outputBlock /etc/hyperledger/configtx/$CHANNEL_NAME.block \
    --tls true \
    --cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem
wait_until "docker exec digi-bank-po_cli bash -c '[ -f /etc/hyperledger/configtx/$CHANNEL_NAME.block ] && exit 0 || exit 1'" 3 5

wait_until "docker exec digi-bank-po_cli peer channel join -b /etc/hyperledger/configtx/$CHANNEL_NAME.block" 3 5
wait_until "docker exec digi-bank-po_cli peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f /etc/hyperledger/config/DigiBankPOMSP_${CHANNEL_NAME}_channel_anchors.tx --tls true --cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem" 3 5

wait_until "docker exec magneto-corp-po_cli peer channel join -b /etc/hyperledger/configtx/$CHANNEL_NAME.block" 3 5
wait_until "docker exec magneto-corp-po_cli peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f /etc/hyperledger/config/MagnetoCorpPOMSP_${CHANNEL_NAME}_channel_anchors.tx --tls true --cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem" 3 5

wait_until "docker exec hedgematic-po_cli peer channel join -b /etc/hyperledger/configtx/$CHANNEL_NAME.block" 3 5
wait_until "docker exec hedgematic-po_cli peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f /etc/hyperledger/config/HedgematicPOMSP_${CHANNEL_NAME}_channel_anchors.tx --tls true --cafile /etc/hyperledger/config/crypto-config/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem" 3 5
