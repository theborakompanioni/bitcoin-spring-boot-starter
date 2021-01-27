[![Build Status](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter.svg?branch=master)](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter)
[![Download](https://jitpack.io/v/theborakompanioni/spring-boot-bitcoin-starter.svg)](https://jitpack.io/#theborakompanioni/spring-boot-bitcoin-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/spring-boot-bitcoin-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE)

spring-boot-bitcoin-starter
===

<p align="center">
    <img src="https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


**Write enterprise Bitcoin applications with Spring Boot.**

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

Also, if you have [bitcoinj](https://github.com/bitcoinj/bitcoinj) in the classpath, it will create a bean
of type `BitcoinjTransactionPublisherFactory` and `BitcoinjBlockPublisherFactory` which will emit `bitcoinj` types for your convenience.


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
org.tbk.lightning.lnd.grpc:
  enabled: true
  rpchost: localhost
  rpcport: 10009
  macaroon-file-path: '/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon'
  cert-file-path: '/lnd/.lnd/tls.cert'
```


### spring-testcontainer

This module contains a fast and easy way to start one or multiple instances of external services within 
docker containers programmatically directly from your application.
Please note, that **these modules are intended to be used in regtest mode only**.

Start and run:
- [x] [Bitcoin Core](https://github.com/bitcoin/bitcoin) with spring-testcontainer-bitcoind-starter
- [x] [lnd](https://github.com/bitcoin/bitcoin) with spring-testcontainer-lnd-starter
- [x] [ElectrumX](https://github.com/spesmilo/electrumx) with spring-testcontainer-electrumx-starter
- [x] [Electrum Personal Server](https://github.com/chris-belcher/electrum-personal-server) with spring-testcontainer-electrum-personal-server-starter
- [x] [Electrum](https://github.com/spesmilo/electrum) with spring-testcontainer-electrum-daemon-starter
- [x] [Tor](https://www.torproject.org/) with [spring-testcontainer-tor-starter](spring-testcontainer/spring-testcontainer-tor-starter)

Most of these spring boot starter modules contain a simple example application. 
They can be used in combination with other modules like [bitcoin-jsonrpc-client](#bitcoin-jsonrpc-client), 
[bitcoin-zeromq-client](#bitcoin-zeromq-client), [lnd-grpc-client](#lnd-grpc-client), etc.


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


### incubator
This subproject is home to all almost-ready modules.

#### tbk-tor
A module containing a spring boot starter for an embedded [Tor daemon](https://www.torproject.org/).
The starter will automatically expose your application as hidden service!

```yaml
org.tbk.tor:
  enabled: true  # whether auto-config should run - default is `true`
  auto-publish-enabled: true # auto publish the web port as hidden service - default is `true`
  working-directory: 'my-tor-directory' # the working directory for tor - default is `tor-working-dir`
  startup-timeout: 30s # max startup duration for tor to successfully start - default is `60s`
```

#### tbk-electrum-daemon-client
A module containing a spring boot starter for a [Electrum daemon](https://github.com/spesmilo/electrum) JSON-RPC API client.
It can be used in combination with [spring-testcontainer-electrum-daemon-starter](#spring-testcontainer)!


### examples
Besides, that most starter modules also have their own example applications, there are also stand-alone 
example applications showing basic usage of the functionality provided by these modules.

- [x] bitcoin-autodca: [Stacking Sats on Kraken: Auto DCA example application](examples/bitcoin-autodca-example-application)
- [x] bitcoin-exchange-rate: [Currency Conversion API example application](examples/bitcoin-exchange-rate-example-application)
- [x] lnd-playground: [Lightning Network Playground example application](examples/lnd-playground-example-application) (using lnd)

Example apps can be started with a single command, e.g.:
```shell
./gradlew -p examples/lnd-playground-example-application bootRun
```

## Development
### Requirements
- java >=11
- docker

A Bitcoin Core Testcontainer running in regtest mode is started for most examples. 
Having access to a Bitcoin Core node running on mainnet is quite useful if you want to try everything.
Optional: A node should publish `rawtx` and `rawblock` messages via zmq for some features to be working.

### Build
```
./gradlew build -x test
```
 
### Test
```
./gradlew test integrationTest
```

Tests in example application modules or modules that start a lot of docker containers 
(modules named "*-example-application" or "spring-testcontainer-*") are excluded from the
default test phase and must be manually enabled if you want to run them.
To run all tests you must pass `-PexampleTest` and `-PtestcontainerTest`:
```
./gradlew test integrationTest -PtestcontainerTest -PexampleTest
```
Be aware this might take several minutes to complete (>= 15 minutes).


### Dependency Checks
```
# verifies checksums of dependencies
./gradlew verifyChecksums
```

```
# calculate checksums of dependencies
./gradlew -q calculateChecksums | grep -v "spring-boot-bitcoin-starter" > checksums.gradle
```


# Resources
- Bitcoin: https://bitcoin.org/en/getting-started
- Lightning Network: https://lightning.network/
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
---
- Bitcoin Core (GitHub): https://github.com/bitcoin/bitcoin
- lnd (GitHub): https://github.com/LightningNetwork/lnd
- ElectrumX Server (GitHub): https://github.com/spesmilo/electrumx
- Electrum Personal Server (GitHub): https://github.com/chris-belcher/electrum-personal-server
- Electrum Client (GitHub): https://github.com/spesmilo/electrum
- XChange (GitHub): https://github.com/knowm/XChange
- bitcoinj (GitHub): https://github.com/bitcoinj/bitcoinj
- Lightningj (GitHub): https://github.com/lightningj-org/lightningj
- ConsensusJ (GitHub): https://github.com/ConsensusJ/consensusj
- JeroMq (GitHub): https://github.com/zeromq/jeromq
- Project Reactor (GitHub): https://github.com/reactor/reactor-core
- Testcontainers (GitHub): https://github.com/testcontainers/testcontainers-java/
- Tor: https://www.torproject.org/
- Protocol Buffers: https://developers.google.com/protocol-buffers


# License
The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
