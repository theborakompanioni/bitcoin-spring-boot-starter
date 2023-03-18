```shell
./gradlew -p spring-testcontainer/spring-testcontainer-cln-example-application bootRun
```

e.g. mine a block manually

```shell
docker exec -it tbk-testcontainer-ruimarinho-bitcoin-core-$id \
    bitcoin-cli -rpcuser=this-is-my-rpc-user99 -rpcpassword=correct_horse_battery_staple_99 -chain=regtest \
    generatetoaddress 1 bcrt1qrnz0thqslhxu86th069r9j6y7ldkgs2tzgf5wx
```
