bitcoin-electrum-shell-example-application
===

A small demo application with bitcoin/electrumx/electrum in regtest mode.

## Build
```shell
./gradlew -p examples/incubator/bitcoin-electrum-shell-example-application bootJar
```

## Run
```shell
SPRING_PROFILES_ACTIVE=development ./examples/incubator/bitcoin-electrum-shell-example-application/build/libs/bitcoin-electrum-shell-example-application-<$version>-boot.jar
# or
SPRING_PROFILES_ACTIVE=mainnet ./examples/incubator/bitcoin-electrum-shell-example-application/build/libs/bitcoin-electrum-shell-example-application-<$version>-boot.jar
```

## Example

### Interactive

```shell
./examples/bitcoin-electrum-shell-example-application/build/libs/bitcoin-electrum-shell-example-application-0.1.0-dev-boot.jar 
nostr:>help
AVAILABLE COMMANDS

Built-In Commands
       help: Display help about available commands
       stacktrace: Display the full stacktrace of the last error.
       clear: Clear the shell screen.
       quit, exit: Exit the shell.
       history: Display or save the history of previously run commands
       version: Show version info
       script: Read and execute commands from a file.

Commands
       check-wallet-tree-file: check balances of wallet tree file
       getaddressbalance: execute command 'getaddressbalance'
       getaddresshistory: execute command 'getaddresshistory'
       getinfo: execute command 'getinfo'
       getaddressunspent: execute command 'getaddressunspent'
       vanity: generate a vanity address
```

#### `getaddressbalance`
```shell
$>getaddressbalance bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl  # regtest eater address
50.00 BTC
# or
$>getaddressbalance 12higDjoCCNXSA95xZMWUdPvXNmkAduhWv
0.00035711 BTC
```

#### `getaddresshistory`
```shell
$>getaddresshistory bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl  # regtest eater address
[ {
  "txHash" : "dc9e376d145d962d3b2e52a83b7f7ef9c390f0227a10173ac993f7ee7d642fa1",
  "height" : 1
} ]
# or
$>getaddresshistory 12higDjoCCNXSA95xZMWUdPvXNmkAduhWv
[ {
  "txHash" : "6f7cf9580f1c2dfb3c4d5d043cdbb128c640e3f20161245aa7372e9666168516",
  "height" : 728
}, {
  "txHash" : "90ff15e5a80593977fb2f6666de2860584d39ebc3a41f65a0a1fdc3a851aefda",
  "height" : 1056
}, [...] ]
```

#### `getaddressunspent`
```shell
$>getaddressunspent bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl  # regtest eater address
{
  "utxos" : [ {
    "height" : 1,
    "txHash" : "dc9e376d145d962d3b2e52a83b7f7ef9c390f0227a10173ac993f7ee7d642fa1",
    "txPos" : 0,
    "value" : {
      "value" : 5000000000,
      "zero" : false
    },
    "address" : "bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl"
  } ],
  "value" : {
    "value" : 5000000000,
    "zero" : false
  },
  "empty" : false
}
# or
$>getaddressunspent 12higDjoCCNXSA95xZMWUdPvXNmkAduhWv
{
  "utxos" : [ {
    "height" : 898336,
    "txHash" : "4c776295b641f078d4a405f71fcf9ac2aae8fb4b18f7a1fc8a132682390946c9",
    "txPos" : 0,
    "value" : {
      "value" : 19855,
      "zero" : false
    },
    "address" : "12higDjoCCNXSA95xZMWUdPvXNmkAduhWv"
  }, [...] ],
  "value" : {
    "value" : 35711,
    "zero" : false
  },
  "empty" : false
}
```

#### `getinfo`
```shell
$>getinfo
{
  "network" : "regtest",
  "path" : "/home/electrum/.electrum/regtest",
  "server" : "host.testcontainers.internal",
  "blockchain_height" : 3,
  "server_height" : 3,
  "spv_nodes" : 1,
  "connected" : true,
  "auto_connect" : false,
  "version" : "4.6.0",
  "fee_estimates" : { }
}
# or
$>getinfo
{
  "network" : "mainnet",
  "path" : "/home/electrum/.electrum",
  "server" : "blockstream.info",
  "blockchain_height" : 909170,
  "server_height" : 909170,
  "spv_nodes" : 1,
  "connected" : true,
  "auto_connect" : false,
  "version" : "4.6.0",
  "fee_estimates" : {
    "10" : 1755,
    "1008" : 1000,
    "144" : 1000,
    "2" : 3156,
    "25" : 1086,
    "5" : 2361
  }
}
```

#### `check-wallet-tree-file`
```shell
$>check-wallet-tree-file regtest.txt
bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl;350.00 BTC;true
bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz;0.00 BTC;false
bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j;0.00 BTC;false
# or
$>check-wallet-tree-file mainnet.txt
12higDjoCCNXSA95xZMWUdPvXNmkAduhWv;0.00035711 BTC;true
342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey;0.00008563 BTC;true
bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak;0.0060157 BTC;true
```

#### `vanity`
```shell
# or
$>vanity --network regtest --address-prefix eat
{
  "address" : "bcrt1qeat2kv2xs5pw7hrhs4n9dwxw3py7j7rfv802wq",
  "mnemonic" : "mercy dawn spare satisfy place aspect eye measure live argue skate scorpion",
  "passphrase" : ""
}
# or
$>vanity --address-type p2pkh
{
"address" : "1AWgukWaE1hcBv3VELsK5aRr8ZTo4NSLwZ",
"mnemonic" : "roast energy copy crouch reason tragic icon letter embody fault mom noble",
"passphrase" : ""
}
# or
$>vanity --network mainnet --address-prefix eater --timeout 5s
Timeout on blocking read for 5000000000 NANOSECONDS
Details of the error have been omitted. You can use the stacktrace command to print the full stacktrace.
```
