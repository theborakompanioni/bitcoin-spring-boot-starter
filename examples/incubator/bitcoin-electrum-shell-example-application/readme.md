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
       getaddressbalance: execute command 'getaddressbalance'
       getinfo: execute command 'getinfo'
```

#### `getaddressbalance`
```shell
electrum:>getaddressbalance bcrt1qgksms8qktns8xajc2ylf6kwrmd6tspfmug72kl  # regtest eater address
50.00 BTC
# or
electrum:>getaddressbalance 12higDjoCCNXSA95xZMWUdPvXNmkAduhWv
0.00035711 BTC
```

#### `getinfo`
```shell
electrum:>getinfo
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
electrum:>getinfo
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
