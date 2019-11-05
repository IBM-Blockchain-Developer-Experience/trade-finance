#!/bin/bash

BASEDIR=$(dirname "$0")

source $BASEDIR/scripts/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")

NETWORK_DOCKER_COMPOSE_DIR=$BASEDIR/docker-compose

echo "###########################"
echo "# SET ENV VARS FOR DOCKER #"
echo "###########################"
set_docker_env $NETWORK_DOCKER_COMPOSE_DIR $APPS_DOCKER_COMPOSE_DIR

echo '############################'
echo '# REMOVE DOCKER CONTAINERS #'
echo '############################'
docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose.yaml -p node down --volumes
docker rm -f $(docker ps -a | grep "dev-peer0.digi-bank-po" | awk '{print $1}')
docker rm -f $(docker ps -a | grep "dev-peer0.magneto-corp-po" | awk '{print $1}')
docker rm -f $(docker ps -a | grep "dev-peer0.hedgematic-po" | awk '{print $1}')

echo '#############################'
echo '# REMOVE DEPLOYED CHAINCODE #'
echo '#############################'
docker rmi $(docker images | grep "^dev-peer0.digi-bank-po" | awk '{print $3}')
docker rmi $(docker images | grep "^dev-peer0.magneto-corp-po" | awk '{print $3}')
docker rmi $(docker images | grep "^dev-peer0.hedgematic-po" | awk '{print $3}')

echo '##################'
echo '# CLEANUP CRYPTO #'
echo '##################'
docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose-cli.yaml up -d
docker exec cli bash -c 'cd /etc/hyperledger/config; rm -rf crypto-config; find . -type f -name "*.tx" -delete; find . -type f -name "*.block" -delete; rm -f core.yaml'
docker-compose -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose-cli.yaml down --volumes

echo '###########################'
echo '# CLEANUP NETWORK DETAILS #'
echo '###########################'
rm -rf $BASEDIR/tradenet_fabric

echo "#############################"
echo "# CLEAN ENV VARS FOR DOCKER #"
echo "#############################"
unset $(cat $NETWORK_DOCKER_COMPOSE_DIR/.env | sed -E 's/(.*)=.*/\1/' | xargs)

echo "#################"
echo "# STOP COMPLETE #"
echo "#################"
