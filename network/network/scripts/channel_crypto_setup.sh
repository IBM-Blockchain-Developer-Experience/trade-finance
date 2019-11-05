#!/bin/bash

helpFunction()
{
   echo ""
   echo "Usage:"
   echo -e "\t-g genesis profile name"
   echo -e "\t-p profile name"
   echo -e "\t-n channel name"
   exit 1 # Exit script after printing help
}

while getopts "p:n:g:" opt
do
   case "$opt" in
      g ) GENESIS="$OPTARG" ;;
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

if [ -z "$GENESIS" ]; then
    read -p "Enter genesis to use: " GENESIS
fi

if [ -z "$PROFILE" ]; then
    read -p "Enter profile to use: " PROFILE
fi

if [ -z "$CHANNEL_NAME" ]; then
    read -p "Enter name of channel: " CHANNEL_NAME
fi

echo "GENERATING CHANNEL CRYPTO: "
echo "GENESIS: $GENESIS"
echo "PROFILE: $PROFILE"
echo "CHANNEL ID: $CHANNEL_NAME"

docker exec cli configtxgen -profile $GENESIS -outputBlock /etc/hyperledger/config/$GENESIS.block
docker exec cli configtxgen -profile $PROFILE -outputCreateChannelTx /etc/hyperledger/config/$PROFILE.tx -channelID $CHANNEL_NAME

docker exec cli configtxgen -profile $PROFILE -outputAnchorPeersUpdate /etc/hyperledger/config/DigiBankPOMSP_${CHANNEL_NAME}_channel_anchors.tx -channelID $CHANNEL_NAME -asOrg DigiBankPOMSP
docker exec cli configtxgen -profile $PROFILE -channelID $CHANNEL_NAME -asOrg DigiBankSPMSP

docker exec cli configtxgen -profile $PROFILE -outputAnchorPeersUpdate /etc/hyperledger/config/MagnetoCorpPOMSP_${CHANNEL_NAME}_channel_anchors.tx -channelID $CHANNEL_NAME -asOrg MagnetoCorpPOMSP
docker exec cli configtxgen -profile $PROFILE -channelID $CHANNEL_NAME -asOrg MagnetoCorpSPMSP

docker exec cli configtxgen -profile $PROFILE -outputAnchorPeersUpdate /etc/hyperledger/config/HedgematicPOMSP_${CHANNEL_NAME}_channel_anchors.tx -channelID $CHANNEL_NAME -asOrg HedgematicPOMSP
docker exec cli configtxgen -profile $PROFILE -channelID $CHANNEL_NAME -asOrg HedgematicSPMSP
docker exec cli configtxgen -profile $PROFILE -channelID $CHANNEL_NAME -asOrg EcoBankSPMSP
docker exec cli cp /etc/hyperledger/fabric/core.yaml /etc/hyperledger/config
docker exec cli sh /etc/hyperledger/config/rename_sk.sh
