[![Build Status](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter.svg?branch=master)](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter)
[![Download](https://jitpack.io/v/theborakompanioni/spring-boot-bitcoin-starter.svg)](https://jitpack.io/#theborakompanioni/spring-boot-bitcoin-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/spring-boot-bitcoin-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE)

spring-boot-bitcoin-starter
===

Spring boot starter projects with convenient dependency descriptors for multiple Bitcoin related modules that you can 
include in your application. Strong focus on integration and regression testing your own application or module.
Included are features for representing, transporting, and performing comprehensive calculations and tests
with Bitcoin in financial applications and monetary computations.
 
**Note**: Most code is still experimental - **do not use in production**.
This project is under active development. Pull requests and issues are welcome.
 
## Modules
### bitcoin-jsr354
Contains a JSR354 compliant `CurrentyUnit` implementation representing Bitcoin.

### spring-jsr354
A module containing a spring boot starter for convenient handling of JSR354 beans.
This module creates an application context aware JSR354 Service Provider (`javax.money.spi.ServiceProvider`)
that provides beans in the application context to be used by JSR354 factories (`javax.money.Monetary`).

### xchange-jsr354
A module containing a spring boot starter for integrating [XChange]( https://github.com/knowm/XChange)
in JSR354 currency conversions. Provides a
`javax.money.convert.ExchangeRateProvider` implementation that uses `org.knowm.xchange.Exchange` beans
to supply exchange rates from popular Bitcoin exchanges.

e.g.
```java
CurrencyConversion btcToUsdConversion = MonetaryConversions.getConversion(ConversionQueryBuilder.of()
    .setBaseCurrency(Monetary.getCurrency("BTC"))
    .setTermCurrency(Monetary.getCurrency("USD"))
    .build());

Money singleBitcoin = Money.of(BigDecimal.ONE, "BTC");
Money singleBitcoinInUsd = singleBitcoin.with(btcToUsdConversion);

log.info("{} equals {}", singleBitcoin, singleBitcoinInUsd);
// e.g. "BTC 1.00 equals USD 13806.90"
```

### bitcoin-zeromq-client
A module containing a spring boot starter for a Bitcoin Core ZeroMq API client.
The starter will automatically create autowireable `ZeroMqMessagePublisherFactory` beans
for every zmq endpoint:

```yaml
org.tbk.bitcoin.zeromq:
  network: mainnet
  zmqpubrawblock: tcp://localhost:28332
  zmqpubrawtx: tcp://localhost:28333
  zmqpubhashblock: tcp://localhost:28334
  zmqpubhashtx: tcp://localhost:28335
```

Here is an example of how to subscribe to messages:
```java
@Slf4j
public final class SubscribeToBitcoinTransactionsViaZeroMqExample {

  @Autowire
  @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory")
  private MessagePublisherFactory<byte[]> rawTxMessageFactory;

  public void start() {
    this.rawTxMessageFactory.create().subscribe(rawTx -> {
      log.info("Got raw transaction: {}", rawTx);
    });
  }
}
```

See the autoconfig class `BitcoinZeroMqClientAutoConfiguration` for more details.

Also, if you have [Bitcoinj](https://github.com/bitcoinj/bitcoinj) in the classpath, it will create a bean
of type `BitcoinjTransactionPublisherFactory` and `BitcoinjBlockPublisherFactory` which will emit Bitcoinj types for your convenience.


### bitcoin-jsonrpc-client
A module containing a spring boot starter for a [ConsensusJ](https://github.com/ConsensusJ/consensusj) Bitcoin Core JSON-RPC API client.
The starter will automatically create an autowireable `BitcoinClient` bean:

```yaml
org.tbk.bitcoin.jsonrpc.client:
  enabled: true
  network: mainnet
  rpchost: http://localhost
  rpcport: 8332
  rpcuser: myrpcuser
  rpcpassword: 'myrpcpassword'
```


### lnd-grpc-client
A module containing a spring boot starter for a [Lightningj](https://www.lightningj.org/) lnd gRPC API client.
The starter will automatically create autowireable `AsynchronousLndAPI` and `SynchronousLndAPI` beans:

```yaml
org.tbk.lightning.lnd.jsonrpc:
  enabled: true
  rpchost: localhost
  rpcport: 10009
  macaroonFilePath: '/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon'
  certFilePath: '/lnd/.lnd/tls.cert'
```


### spring-testcontainer

#### spring-testcontainer-bitcoind-starter
A fast any easy way to start one or multiple Bitcoin Core Daemons within docker containers programmatically within
your application.
Please note, that **this module is intended to be used in regtest mode only**.

This spring boot starter module can be used in combination with
[bitcoin-jsonrpc-client](#bitcoin-jsonrpc-client) and [bitcoin-zeromq-client](#bitcoin-zeromq-client).

#### spring-testcontainer-lnd-starter
A fast any easy way to start one or multiple lnd Daemons within docker containers programmatically within
your application.
Please note, that **this module is intended to be used in regtest mode** and currently
only works in combination with `spring-testcontainer-bitcoind-starter`.

#### spring-testcontainer-electrumx-starter
A fast any easy way to start one or multiple electrumx instances within docker containers programmatically within
your application.
It can be used in combination with `spring-testcontainer-bitcoind-starter`!
Please note, that **this module is intended to be used in regtest mode**.

#### spring-testcontainer-electrum-daemon-starter
A fast any easy way to start one or multiple electrum-daemon Daemons within docker containers programmatically within
your application.
It can be used in combination with `spring-testcontainer-electrumx-starter`!
Please note, that **this module is intended to be used in regtest mode**.

### spring-bitcoin-fee
A generalized and extensible interface of multiple Bitcoin Fee Recommendation APIs.
The following providers are implemented:
- [x] Bitcoin Core JSON-RPC Api (`estimatestmartfee`)
- [x] bitcoiner.live API
- [x] Bitgo API
- [x] Bitcore API
- [x] Blockchain.info API (deprecated - will be removed as it is not compatible with "block target" recommendations)
- [x] Blockchair API
- [x] BlockCypher API
- [x] Blockstream.info API
- [x] BTC.com API
- [x] earn.com API

### examples
There are also some example applications showing basic usage of the functionality provided by these modules.

#### bitcoin-exchange-rate-example-application
Start the application with
```shell
./gradlew -p examples/bitcoin-exchange-rate-example-application bootRun
```

... and open the following url in your browser:
`http://localhost:8080/api/v1/exchange/latest?base=BTC&target=USD&provider=KRAKEN`

Example output (2020-11-08):
```json
{
  "base" : "BTC",
  "rates" : [ {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "15400.00000",
    "meta" : {
      "ask" : "15400.00000",
      "bid" : "15399.90000",
      "high" : "15450.00000",
      "javax.money.convert.RateType" : "DEFERRED",
      "last" : "15400.00000",
      "low" : "14365.00000",
      "open" : "14837.40000",
      "provider" : "KRAKEN",
      "providerDescription" : "Kraken is a Bitcoin exchange operated by Payward, Inc.",
      "providerName" : "Kraken",
      "quoteVolume" : "119845666.5998900000000",
      "rateTypes" : [ "DEFERRED" ],
      "volume" : "7782.18614285",
      "vwap" : "14916.50557"
    },
    "provider" : "KRAKEN",
    "target" : "USD",
    "type" : "DEFERRED"
  } ]
}
```

#### lnd-playground-example-application
Start the application with
```shell
./gradlew -p examples/lnd-playground-example-application bootRun
```

Example console output:
```
2020-12-10 00:48:45.538  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : Started LndContainerExampleApplication in 258.23 seconds (JVM running for 258.867)
2020-12-10 00:48:45.969  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : =================================================
2020-12-10 00:48:45.969  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : [lnd] identity_pubkey: 02c90d9086299b6f446365daff74fa5690b90a2ba18c9a077feeeeff73c5eabe4a
2020-12-10 00:48:45.969  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : [lnd] alias: tbk-lnd-example-application
2020-12-10 00:48:45.970  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : [lnd] version: 0.11.1-beta commit=v0.11.1-beta-4-g3c4471f8818a07e63864d39a1c3352ce19e8f31d
2020-12-10 00:48:45.997  INFO 37837 --- [  restartedMain] t.s.t.l.e.LndContainerExampleApplication : =================================================
2020-12-10 00:48:52.631 DEBUG 37837 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT9.962S
2020-12-10 00:48:52.645  INFO 37837 --- [-pub-63f6c6f2-0] l.e.LndContainerExampleApplicationConfig : [bitcoind] new best block: 53d4d9117159a9359f14828b5c432d23400f30250c87e1fb9d3b25e163b0d9d2
2020-12-10 00:48:52.906  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : =================================================
2020-12-10 00:48:52.906  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] block height: 1
2020-12-10 00:48:52.906  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] block hash: 53d4d9117159a9359f14828b5c432d23400f30250c87e1fb9d3b25e163b0d9d2
2020-12-10 00:48:52.906  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] best header timestamp: 1607557732
2020-12-10 00:48:52.906  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : =================================================
2020-12-10 00:49:01.653 DEBUG 37837 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT4.701S
2020-12-10 00:49:01.664  INFO 37837 --- [-pub-63f6c6f2-0] l.e.LndContainerExampleApplicationConfig : [bitcoind] new best block: 1745d662e4a06eaa4ce86ee0631a96454aeb43e344518b8a3c64728170aa4c3e
2020-12-10 00:49:01.917  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : =================================================
2020-12-10 00:49:01.917  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] block height: 2
2020-12-10 00:49:01.917  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] block hash: 1745d662e4a06eaa4ce86ee0631a96454aeb43e344518b8a3c64728170aa4c3e
2020-12-10 00:49:01.917  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : [lnd] best header timestamp: 1607557741
2020-12-10 00:49:01.917  INFO 37837 --- [-pub-63f6c6f2-0] t.s.t.l.e.LndContainerExampleApplication : =================================================
```


## Development
### Requirements
- java >=11
- docker

To run all examples you need access to a Bitcoin Core node.

Optional: To enable all features the node should publish `rawtx` and `rawblock` messages via zmq.
  
### Build
```
./gradlew clean build
```

# Resources
- Bitcoin: https://bitcoin.org/en/getting-started
- Lightning Network: https://lightning.network/
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
---
- lnd (GitHub): https://github.com/LightningNetwork/lnd
- ElectrumX Server (GitHub): https://github.com/spesmilo/electrumx
- Electrum Client (GitHub): https://github.com/spesmilo/electrum
- XChange (GitHub): https://github.com/knowm/XChange
- ConsensusJ (GitHub): https://github.com/ConsensusJ/consensusj
- JeroMq (GitHub): https://github.com/zeromq/jeromq
- Bitcoinj (GitHub): https://github.com/bitcoinj/bitcoinj
- Project Reactor (GitHub): https://github.com/reactor/reactor-core
- Testcontainers (GitHub): https://github.com/testcontainers/testcontainers-java/
- Protocol Buffers: https://developers.google.com/protocol-buffers

# License
The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
