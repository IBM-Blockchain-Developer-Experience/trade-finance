# Trade Finance Demo

## Pre requisites

- git
- jq
- docker
- docker-compose
- node + npm
- typescript
- python
- make
- g++
- [blockchain-node-generator@1.0.0](https://github.com/liam-grace/blockchain-node-generator) (optional)

## Setting up the demo

Build the CLI tools and REST docker images:

```bash
./network/tf_setup.sh
```

## Starting the demo network

Start up the fabric network, PO and SP REST servers:

```bash
./network/tf_start.sh
```

You can find instructions on interacting with the demo [here](./RunThrough.md).

## Stopping the demo network

Remove the fabric network and stop the PO and SP REST servers:

```bash
./network/tf_stop.sh
```

## Tearing down the demo

Remove the build for the CLI tools and REST docker images:

```bash
./network/tf_teardown.sh
```

## Importing env.json file into the IBM Blockchain Platform Visual Studio Code Extension

To import `network/env.json` into the IBM Blockchain Platform Visual Studio Code Extension, select it when adding an environment

### Wallets

Import all of the wallets from `network/wallets/` into the extension
