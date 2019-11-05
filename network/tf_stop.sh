#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
CUR_DIR=$(pwd)

source $BASEDIR/network/scripts/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")

rm -rf $BASEDIR/wallets
rm -rf $BASEDIR/tmp

bash $BASEDIR/network/network_stop.sh

echo "#################"
echo "# TEARDOWN APIS #"
echo "#################"
docker-compose -f $BASEDIR/apps/docker-compose/docker-compose.yaml -p node down --volumes
