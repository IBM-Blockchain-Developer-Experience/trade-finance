#!/bin/bash

BASEDIR=$(dirname "$0")

source $BASEDIR/utils.sh
BASEDIR=$(get_full_path "$BASEDIR")

CLI_DIR=$BASEDIR/../../cli_tools

OUTPUT_DIR="${BASEDIR}/../tradenet_fabric"

mkdir -p "$OUTPUT_DIR"

LOCAL_MSP_DIR="${BASEDIR}/../crypto-material/crypto-config"
DOCKER_MSP_DIR="/msp"

LOCAL_URL="localhost"

ORDERER_DOCKER_URL="orderer.example.com"
ORDERER_PORT="7050"

PEER_DOCKER_PORT="7051"
PEER_DOCKER_EVENT_PORT="7053"
CA_DOCKER_PORT="7054"

ORGS=("DigiBankPO" "DigiBankSP" "MagnetoCorpPO" "MagnetoCorpSP" "HedgematicPO" "HedgematicSP" "EcoBankSP") # MUST MATCH STARTS OF VARS ABOVE TO MAKE LOOPS WORK
EP_ORGS=("DigiBankPO" "MagnetoCorpPO" "HedgematicPO")
TYPES=("DOCKER" "LOCAL") # MUST MATCH THE _TYPE IN THE ABOVE VARS TO MAKE LOOPS WORK

DigiBankSP_TRUSTED_POS=("DigiBankPO")
MagnetoCorpSP_TRUSTED_POS=("MagnetoCorpPO")
HedgematicSP_TRUSTED_POS=${EP_ORGS[@]}
EcoBankSP_TRUSTED_POS=${EP_ORGS[@]}

LOCAL_CONNECTION_NAME="local_connection.json"
DOCKER_CONNECTION_NAME="connection.json"

index_of_org() {
    TOFIND=${1}

    COUNTER=0
    for EL in "${ORGS[@]}"; do
        if [ "$TOFIND" = "$EL" ]; then
            echo "$COUNTER"
            break
        fi

        COUNTER=$((COUNTER + 1))
    done
}

generate_small_name() {
    ORG_NAME=${1}

    echo $(echo ${ORG_NAME:0:$((${#ORG_NAME} - 2))} | perl -ne 'print lc(join("-", split(/(?=[A-Z])/)))')-$(echo ${ORG_NAME:$((${#ORG_NAME} - 2))} | tr '[:upper:]' '[:lower:]')
}

generate_org_json() {
    echo $(
        jq -n \
        --arg name "${1}" \
        --arg smallName "${2}" \
        '{"name": $name, "smallName": $smallName}'
    )
}

generate_node_json() {
    echo $(
        jq -n \
        --arg name "${1}" \
        --arg url "${2}" \
        --arg port "${3}" \
        --argjson org "${4}" \
        '{"name": $name, "url": $url, "port": $port, "org": $org}'
    )
}

generate_peer_json() {
    OFFSET=${2}
    ORG=${3}

    PEER_NAME="peer0.${1}.com"
    PEER_URL=$PEER_NAME
    PEER_PORT=$((PEER_DOCKER_PORT + (OFFSET * 1000)))
    PEER_EVENT_PORT=$((PEER_DOCKER_EVENT_PORT + (OFFSET * 1000)))

    if [ $TYPE == "LOCAL" ]; then
        PEER_URL=$LOCAL_URL
    fi

    NODE_JSON=$(generate_node_json ${PEER_NAME} ${PEER_URL} ${PEER_PORT} "${ORG}")

    echo $(echo $NODE_JSON | jq --arg eventPort ${PEER_EVENT_PORT} '. + {"eventPort": $eventPort}')
}

generate_ca_json() {
    OFFSET=${2}
    ORG=${3}

    CA_NAME="tlsca.${1}.com"
    CA_URL=$CA_NAME
    CA_PORT=$CA_DOCKER_PORT

    if [ $TYPE == "LOCAL" ]; then
        CA_URL=$LOCAL_URL
        CA_PORT=$((CA_DOCKER_PORT + (OFFSET * 1000)))
    fi

    echo $(generate_node_json ${CA_NAME} ${CA_URL} ${CA_PORT} "${ORG}")
}

generate_orderer_json() {
    ORG=${2}

    ORDERER_NAME="orderer.${1}.com"
    ORDERER_URL=$ORDERER_NAME
    ORDERER_PORT=$ORDERER_PORT

    echo $(generate_node_json ${ORDERER_NAME} ${ORDERER_URL} ${ORDERER_PORT} "${ORG}")
}

COUNTER=0

for ORG in "${ORGS[@]}"; do

    HASHES=$(printf "%-${#ORG}s" "#")
    HASH_PADDING=$(echo "${HASHES// /#}")

    echo "####################################$HASH_PADDING##"
    echo "# GENERATING CONNECTION PROFILE FOR $ORG #"
    echo "####################################$HASH_PADDING##"

    for TYPE in "${TYPES[@]}"; do

        MSP_DIR="${TYPE}_MSP_DIR"

        ORG_NAME="$ORG"
        ORG_SMALL_NAME=$(generate_small_name $ORG_NAME)
        ORG_STRING=$(generate_org_json $ORG_NAME $ORG_SMALL_NAME)

        CA_STRING=$(generate_ca_json $ORG_SMALL_NAME $COUNTER "${ORG_STRING}")
        CAS_STRING=$(
                jq -n --argjson ca "$CA_STRING" '[$ca]'
        )

        if [ "${ORG_NAME:$((${#ORG_NAME} - 2))}" == "PO" ]; then

            PEER_STRING=$(generate_peer_json $ORG_SMALL_NAME $COUNTER "${ORG_STRING}")

            PEERS_STRING=$(
                jq -n --argjson peer "$PEER_STRING" '[$peer]'
            )
        else
            PEERS_STRING="[]"

            TRUSTED_POS_VAR_NAME="${ORG}_TRUSTED_POS[@]"
            TRUSTED_POS=${!TRUSTED_POS_VAR_NAME}

            for TRUSTED_PO in ${TRUSTED_POS}; do
                PEER_ORG_SMALL_NAME=$(generate_small_name $TRUSTED_PO)
                PEER_ORG_STRING=$(generate_org_json $TRUSTED_PO $PEER_ORG_SMALL_NAME)

                INDEX=$(index_of_org $TRUSTED_PO)

                PEER_STRING=$(generate_peer_json $PEER_ORG_SMALL_NAME $INDEX "${PEER_ORG_STRING}" $PEER_EVENT_PORT)

                PEERS_STRING=$(echo $PEERS_STRING | jq --argjson peer "$PEER_STRING" '. + [$peer]')
            done
        fi

        ORDERER_ORG=$(generate_org_json example example)
        ORDERER_STRING=$(generate_orderer_json example "$ORDERER_ORG")

        ORDERERS_STRING=$(
            jq -n --argjson orderer "$ORDERER_STRING" '[$orderer]'
        )

        JSON_STRING=$(
            jq -n \
            --arg mspDir "${!MSP_DIR}" \
            --argjson org "${ORG_STRING}" \
            --argjson peers "${PEERS_STRING}" \
            --argjson cas "${CAS_STRING}" \
            --argjson orderers "${ORDERERS_STRING}" \
            '{"org": $org, "peers": $peers, "cas": $cas, "orderers": $orderers, "mspDir": $mspDir}'
        )

        OUTPUT_FILE_END="${TYPE}_CONNECTION_NAME"
        OUTPUT_FILE="${ORG_NAME}_${!OUTPUT_FILE_END}"

        node $CLI_DIR/dist/index.js connection-profile-generate --config "$JSON_STRING" --output "$OUTPUT_DIR/$OUTPUT_FILE"
    done

    COUNTER=$((COUNTER + 1))

done
