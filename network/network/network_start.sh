#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
CUR_DIR=$(pwd)

source $BASEDIR/scripts/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")

NETWORK_DOCKER_COMPOSE_DIR=$BASEDIR/docker-compose

echo "###########################"
echo "# SET ENV VARS FOR DOCKER #"
echo "###########################"
set_docker_env $NETWORK_DOCKER_COMPOSE_DIR

echo "###################"
echo "# GENERATE CRYPTO #"
echo "###################"
docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose-cli.yaml up -d

docker exec cli cryptogen generate --config=/etc/hyperledger/config/crypto-config.yaml --output /etc/hyperledger/config/crypto-config

bash $BASEDIR/scripts/channel_crypto_setup.sh -g TradenetGenesis -p TradenetPurchase -n tradenetpurchase
bash $BASEDIR/scripts/channel_crypto_setup.sh -g TradenetGenesis -p TradenetFinance -n tradenetfinance

docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose-cli.yaml down --volumes

echo "#################"
echo "# SETUP NETWORK #"
echo "#################"
docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose.yaml -p node up -d

echo "################"
echo "# CHANNEL INIT #"
echo "################"
bash $BASEDIR/scripts/channel_join.sh -p TradenetPurchase -n tradenetpurchase
bash $BASEDIR/scripts/channel_join.sh -p TradenetFinance -n tradenetfinance

echo "####################"
echo "# ENROLLING ADMINS #"
echo "####################"
mkdir -p "$BASEDIR/tmp"

IDENTITIES_FOLDER=$BASEDIR/tradenet_fabric/identities

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/DigiBankPO/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
DigiBankPO_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
DigiBankPO_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.digi-bank-po.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.digi-bank-po.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.digi-bank-po.com-cert.pem"
docker exec tlsca.digi-bank-po.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.digi-bank-po.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.digi-bank-po.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$DigiBankPO_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$DigiBankPO_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/DigiBankSP/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
DigiBankSP_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
DigiBankSP_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.digi-bank-sp.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.digi-bank-sp.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.digi-bank-sp.com-cert.pem"
docker exec tlsca.digi-bank-sp.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.digi-bank-sp.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.digi-bank-sp.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$DigiBankSP_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$DigiBankSP_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/MagnetoCorpPO/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
MagnetoCorpPO_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
MagnetoCorpPO_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.magneto-corp-po.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.magneto-corp-po.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.magneto-corp-po.com-cert.pem"
docker exec tlsca.magneto-corp-po.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.magneto-corp-po.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.magneto-corp-po.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$MagnetoCorpPO_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$MagnetoCorpPO_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/MagnetoCorpSP/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
MagnetoCorpSP_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
MagnetoCorpSP_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.magneto-corp-sp.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.magneto-corp-sp.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.magneto-corp-sp.com-cert.pem"
docker exec tlsca.magneto-corp-sp.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.magneto-corp-sp.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.magneto-corp-sp.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$MagnetoCorpSP_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$MagnetoCorpSP_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/HedgematicPO/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
HedgematicPO_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
HedgematicPO_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.hedgematic-po.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.hedgematic-po.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.hedgematic-po.com-cert.pem"
docker exec tlsca.hedgematic-po.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.hedgematic-po.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.hedgematic-po.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$HedgematicPO_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$HedgematicPO_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/HedgematicSP/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
HedgematicSP_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
HedgematicSP_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.hedgematic-sp.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.hedgematic-sp.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.hedgematic-sp.com-cert.pem"
docker exec tlsca.hedgematic-sp.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.hedgematic-sp.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.hedgematic-sp.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$HedgematicSP_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$HedgematicSP_ADMIN_KEY"

ORG_ADMIN_FOLDER="$IDENTITIES_FOLDER/EcoBankSP/admin"
mkdir -p "$ORG_ADMIN_FOLDER"
EcoBankSP_ADMIN_CERT="$ORG_ADMIN_FOLDER/cert.pem"
EcoBankSP_ADMIN_KEY="$ORG_ADMIN_FOLDER/key.pem"

FABRIC_CA_CLIENT_HOME=/root/fabric-ca/clients/admin

docker exec tlsca.eco-bank-sp.com bash -c "fabric-ca-client enroll -u https://admin:adminpw@tlsca.eco-bank-sp.com:7054 --tls.certfiles /etc/hyperledger/fabric-ca-server-tlsca/tlsca.eco-bank-sp.com-cert.pem"
docker exec tlsca.eco-bank-sp.com bash -c "cd $FABRIC_CA_CLIENT_HOME/msp/keystore; find ./ -name '*_sk' -exec mv {} key.pem \;"
docker cp tlsca.eco-bank-sp.com:$FABRIC_CA_CLIENT_HOME/msp/signcerts/cert.pem "$BASEDIR/tmp"
docker cp tlsca.eco-bank-sp.com:$FABRIC_CA_CLIENT_HOME/msp/keystore/key.pem "$BASEDIR/tmp"

mv "$BASEDIR/tmp/cert.pem" "$EcoBankSP_ADMIN_CERT"
mv "$BASEDIR/tmp/key.pem" "$EcoBankSP_ADMIN_KEY"


rm -rf $BASEDIR/tmp

echo "###############################"
echo "# GENERATE CONNECTION PROFILE #"
echo "###############################"

bash $BASEDIR/scripts/generate_connection_profile.sh

echo "#############################"
echo "# CLEAN ENV VARS FOR DOCKER #"
echo "#############################"
unset $(cat $NETWORK_DOCKER_COMPOSE_DIR/.env | sed -E 's/(.*)=.*/\1/' | xargs)
