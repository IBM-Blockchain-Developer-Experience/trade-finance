#!/bin/bash

build_sub_module () {
    dir=$1
    if [ -d "$dir" ]; then
        cd $dir
        if [ -n "$(ls -A deps)" ]; then
            for d in deps/*; do
                echo "HERE " $(pwd)
                cd $d
                echo "=> Building $d jar"

                build_sub_module $d
                ./gradlew build shadowjar
                mkdir -p $BASE/libs
                mv build/libs/* $BASE/libs
                cd $BASE
            done
        fi
    fi
}

echo "=> Pulling git submodules"
git submodule update --init --recursive

BASE=$(pwd)
echo "=> Building .jar files"
build_sub_module $BASE
