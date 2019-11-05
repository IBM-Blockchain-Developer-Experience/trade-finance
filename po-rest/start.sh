#!/bin/bash

REQUIRED_ENV_VARS=("WALLET_PATH" "CONNECTION_PROFILE_PATH" "FABRIC_ORGANISATION" "FABRIC_IDENTITY" "PORT" "PREFERRED_PEER")
MISSING=false

for REQUIRED in "${REQUIRED_ENV_VARS[@]}"; do
    if [ -z "${!REQUIRED}" ]; then
        echo "Missing required environment variable $REQUIRED"
        MISSING=true
    fi
done

if [ "$MISSING" = true ]; then
    exit 1
fi

./bin/po-rest -w "$WALLET_PATH" -c "$CONNECTION_PROFILE_PATH" -o "$FABRIC_ORGANISATION" -i "$FABRIC_IDENTITY" -p $PORT -j "$PREFERRED_PEER" -u "0.0.0.0"
