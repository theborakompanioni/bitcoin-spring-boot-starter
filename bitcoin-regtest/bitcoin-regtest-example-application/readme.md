bitcoin-regtest-example-application
===

A small demo application with bitcoin/electrumx/electrum in regtest mode.

1. Start services with
```shell
docker-compose up
```

2. Start application with
```shell
./gradlew -p bitcoin-regtest/bitcoin-regtest-example-application bootRun
```

Example log output:
```
2021-05-12 00:50:44.460 DEBUG 26485 --- [stMiner RUNNING] o.t.b.regtest.BitcoindRegtestMinerImpl   : Trying to mine 1 block(s) with coinbase reward for address bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz
2021-05-12 00:50:44.469 DEBUG 26485 --- [stMiner RUNNING] o.t.b.regtest.BitcoindRegtestMinerImpl   : Mined 1 blocks with coinbase reward for address bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz
2021-05-12 00:50:44.470 DEBUG 26485 --- [stMiner RUNNING] .c.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT4.031S
2021-05-12 00:50:44.471  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Received zeromq message: 20 - 15a3aef05af081342ea4c0434a3b73644a5433ef64d5ddc63d27e1e170930fcd
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : ============================
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Bitcoin Core (/Satoshi:0.21.1(tbkdevbitcoindregtest)/) Status
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Chain: regtest
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Connections: 0
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Headers: 425
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Blocks: 425
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Best block hash: 15a3aef05af081342ea4c0434a3b73644a5433ef64d5ddc63d27e1e170930fcd
2021-05-12 00:50:44.516  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Difficulty: 0.0000000004656542373906925
2021-05-12 00:50:44.517  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : UTXO: 425 (12775.00 BTC)
2021-05-12 00:50:44.517  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : ============================
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : ============================
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Electrum Daemon (3.3.8) Status
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Connected: true
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Blockheight: 423/423
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Current wallet: /home/electrum/.electrum/regtest/wallets/default_wallet
2021-05-12 00:50:44.521  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Wallet synchronized: true
2021-05-12 00:50:44.601  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Transactions: 423
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : Balance: 12750.00 BTC total
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication :          11500.00 BTC confirmed
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication :          0.00 BTC unconfirmed
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication :          11500.00 BTC spendable
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication :          1250.00 BTC unmatured
2021-05-12 00:50:44.602  INFO 26485 --- [-pub-4d0a86fd-0] t.b.r.e.BitcoinRegtestExampleApplication : ============================
````