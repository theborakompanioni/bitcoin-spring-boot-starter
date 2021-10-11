bitcoin-payment-request-example-application
===

## Run
```shell script
./gradlew -p examples/incubator/bitcoin-payment-request-example-application bootRun
```

### interact with local electrum
1. Find the port of electrumx via docker
```shell
docker ps
```

Example output:
```
CONTAINER ID        IMAGE                                   COMMAND                   CREATED             STATUS              PORTS                                                                                                                                                                                                          NAMES
a82b555c724e        lukechilds/electrumx:v1.15.0            "init"                    4 minutes ago       Up 3 minutes        0.0.0.0:33378->8000/tcp, 0.0.0.0:33377->50001/tcp, 0.0.0.0:33376->50002/tcp, 0.0.0.0:33375->50004/tcp                                                                                                          tbk-testcontainer-lukechilds-electrumx-2f43982c
```
Use the port that is mapped to `50001`: `0.0.0.0:33377->50001/tcp` -> `33377`

2. Start an electrum daemon in regtest mode connecting to the electrumx server:
```shell
./electrum --regtest --oneserver --server 127.0.0.1:33377:t
```

3. Create a regtest wallet with the seed taken from the `application.yml` file

# Resources
- Lightning Network: https://lightning.network/
- lnd (GitHub): https://github.com/LightningNetwork/lnd
- LightningJ (GitHub): https://github.com/lightningj-org/lightningj
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules
- jMolecules - Technology integrations (GitHub): https://github.com/xmolecules/jmolecules-integrations
