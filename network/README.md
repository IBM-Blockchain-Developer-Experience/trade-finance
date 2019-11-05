# Trade Finance Demo network

Set of scripts to set up the network for the Trade Finance demo.

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
- [blockchain-node-generator@1.0.0-alpha.4](https://github.com/liam-grace/blockchain-node-generator) (optional)

## Setting up the demo

Build the CLI tools and REST docker images:

```bash
./tf_setup.sh
```

## Starting the demo network

Start up the fabric network, PO and SP REST servers:

```bash
./tf_start.sh
```

You can find instructions on interacting with the demo [here](../RunThrough.md).

## Stopping the demo network

Remove the fabric network and stop the PO and SP REST servers:

```bash
./tf_stop.sh
```

## Tearing down the demo

Remove the build for the CLI tools and REST docker images:

```bash
./tf_teardown.sh
```
