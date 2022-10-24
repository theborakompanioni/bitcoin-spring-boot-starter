
```shell
./gradlew -p spring-testcontainer/spring-testcontainer-bitcoind-example-application bootRun
```

e.g. mine a block manually
```shell
docker exec -it tbk-testcontainer-ruimarinho-bitcoin-core-$id \
    bitcoin-cli -rpcuser=myrpcuser -rpcpassword=correcthorsebatterystaple -chain=regtest \
    generatetoaddress 1 bcrt1qrnz0thqslhxu86th069r9j6y7ldkgs2tzgf5wx
```
