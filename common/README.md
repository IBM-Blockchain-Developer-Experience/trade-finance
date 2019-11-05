# common
Common code used between clients in the Trade Finance Demo 

## Installation

Installation will pull dependencies and create a .jar file inside `build/libs/`

```bash
gradle build shadowjar
```

## Setup

Copy a connection profile named `local_fabric_connection.json` and a wallet named `wallet` into `src/fixtures`

## Usage

### Submit a transaction

```bash
java -jar build/libs/FabricProxyHarness.jar call submit ATransaction '["This is an argument"]'
```

### Evaluate a transaction

```bash
java -jar build/libs/FabricProxyHarness.jar call evaluate ATransaction '["This is an argument"]'
```

### Crate a block event listener

```bash
java -jar build/libs/FabricProxyHarness.jar listen admin block
```

### Crate a contract event listener

```bash
java -jar build/libs/FabricProxyHarness.jar listen admin contract A_TRANSACTION_EVENT
```
