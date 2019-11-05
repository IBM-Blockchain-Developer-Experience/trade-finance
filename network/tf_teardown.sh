#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
CUR_DIR=$(pwd)

source $BASEDIR/network/scripts/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")
NETWORK_DOCKER_COMPOSE_DIR=$BASEDIR/network/docker-compose
APPS_DOCKER_COMPOSE_DIR=$BASEDIR/apps/docker-compose

ALIVE_FABRIC_DOCKER_IMAGES=$(docker-compose --log-level ERROR -f $NETWORK_DOCKER_COMPOSE_DIR/docker-compose.yaml -p node ps -q | wc -l)
ALIVE_APP_DOCKER_IMAGES=$(docker-compose --log-level ERROR -f $APPS_DOCKER_COMPOSE_DIR/docker-compose.yaml -p node ps -q | wc -l)
if [ "$ALIVE_FABRIC_DOCKER_IMAGES" -ne 0 ] || [ "$ALIVE_APP_DOCKER_IMAGES" -ne 0 ]; then
    echo "###################################"
    echo "# STOP NOT COMPLETE. RUNNING STOP #"
    echo "###################################"

    $BASEDIR/tf_stop.sh
fi

echo '#####################'
echo '# CLEANUP CLI_TOOLS #'
echo '#####################'
rm -rf $BASEDIR/cli_tools/node_modules
rm -f $BASEDIR/cli_tools/package-lock.json
rm -rf $BASEDIR/cli_tools/dist

echo "########################"
echo "# REMOVE DOCKER IMAGES #"
echo "########################"
docker rmi tradefinance/porest
docker rmi tradefinance/sprest
