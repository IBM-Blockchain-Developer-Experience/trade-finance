#!/bin/bash

set -e

BASEDIR=$(dirname "$0")

source $BASEDIR/network/scripts/utils.sh

BASEDIR=$(get_full_path "$BASEDIR")
MODULEDIR=$(get_full_path "$BASEDIR/..")

mkdir -p "$BASEDIR/tmp"

echo "###################"
echo "# BUILD CLI_TOOLS #"
echo "###################"
cd $BASEDIR/cli_tools
npm install
npm rebuild
npm run build
cd $BASEDIR

echo "##########################"
echo "# BUILDING DOCKER IMAGES #"
echo "##########################"
docker build -f $MODULEDIR/po-rest/Dockerfile -t tradefinance/porest $MODULEDIR > $BASEDIR/tmp/porest_docker_build.log &
docker build -f $MODULEDIR/sp-rest/Dockerfile -t tradefinance/sprest $MODULEDIR > $BASEDIR/tmp/sprest_docker_build.log &

wait

rm -rf "$BASEDIR/tmp"
