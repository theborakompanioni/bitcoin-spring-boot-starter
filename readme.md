[![Build](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/actions/workflows/build.yml)
[![Download](https://jitpack.io/v/theborakompanioni/bitcoin-spring-boot-starter.svg)](https://jitpack.io/#theborakompanioni/bitcoin-spring-boot-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/bitcoin-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/LICENSE)


<p align="center">
    <img src="https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


bitcoin-spring-boot-starter
===

**Write enterprise Bitcoin applications with Spring Boot.**

Spring boot starter projects with convenient dependency descriptors for multiple Bitcoin related modules that you can 
import into your application. Strong focus on integration and regression testing your own application or module.
Included are features for representing, transporting, and performing comprehensive calculations and tests with 
Bitcoin in financial applications and computations.
 
**Note**: Most code is still experimental - **do not use in production**.
This project is under active development. Pull requests and issues are welcome.


## Table of Contents

- [Install](#install)
- [Modules](#modules)
- [Examples](#examples)
- [Development](#development)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)


## Install

Simply define JitPack as an artifact repository and add the desired modules as dependencies. 
See [bitcoin-spring-boot-starter on JitPack](https://jitpack.io/#theborakompanioni/bitcoin-spring-boot-starter) 
to find the most recent releases. The examples below import `bitcoin-jsonrpc-client-starter` but you can import 
any module by its name.

### Gradle
```groovy
repositories {
    maven {
        // needed for bitcoin-spring-boot-starter packages
        url 'https://jitpack.io'
    }
}
```

```groovy
dependencies {
    implementation "com.github.theborakompanioni.bitcoin-spring-boot-starter:bitcoin-jsonrpc-client-starter:${bitcoinSpringBootStarterVersion}"
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.theborakompanioni.bitcoin-spring-boot-starter</groupId>
    <artifactId>bitcoin-jsonrpc-client-starter</artifactId>
    <version>${bitcoinSpringBootStarter.version}</version>
</dependency>
```


## Modules

### bitcoin-jsonrpc-client
A module containing a spring boot starter for a [ConsensusJ](https://github.com/ConsensusJ/consensusj) Bitcoin Core JSON-RPC API client.
The starter will automatically create an injectable `BitcoinClient` bean:

```yaml
org.tbk.bitcoin.jsonrpc.client:
  enabled: true
  network: mainnet
  rpchost: http://localhost
  rpcport: 8332
  rpcuser: myrpcuser
  rpcpassword: 'myrpcpassword'
```


### bitcoin-zeromq-client
A module containing a spring boot starter for a Bitcoin Core ZeroMq API client.
The starter will automatically create injectable `ZeroMqMessagePublisherFactory` beans for every zmq endpoint:

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


### lnd-grpc-client
A module containing a spring boot starter for a [Lightningj](https://www.lightningj.org/) lnd gRPC API client.
The starter will automatically create injectable `AsynchronousLndAPI` and `SynchronousLndAPI` beans:

```yaml
org.tbk.lightning.lnd.grpc:
  enabled: true
  rpchost: localhost
  rpcport: 10009
  macaroon-file-path: '/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon'
  cert-file-path: '/lnd/.lnd/tls.cert'
```


### bitcoin-fee
A generalized and extensible interface of multiple Bitcoin Fee Recommendation APIs.
The following providers are available out of the box:
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
- [x] mempool.space API


### spring-xchange
A module containing a spring boot starter for automatically creating and configuring
[XChange]( https://github.com/knowm/XChange) beans!
This starter makes it easy to fetch the current price of bitcoin, programmatically place orders, withdraw your bitcoin
or manage your account!

```yaml
org.tbk.xchange:
  enabled: true # whether auto-config should run - default is `true`
  specifications: # provide specifications for all exchange you want to use - default is empty (no beans created)
    krakenExchange:
      exchange-class: org.knowm.xchange.kraken.KrakenExchange
      api-key: 'your-api-key' # change this value to your api key
      secret-key: 'your-secret-key' #  change this value to your secret key
```


### spring-tor
A module containing a spring boot starter for an embedded [Tor daemon](https://www.torproject.org/).
The starter will automatically expose your application as hidden service!

```yaml
org.tbk.tor:
  enabled: true  # whether auto-config should run - default is `true`
  auto-publish-enabled: true # auto publish the web port as hidden service - default is `true`
  working-directory: 'my-tor-directory' # the working directory for tor - default is `tor-working-dir`
  startup-timeout: 30s # max startup duration for tor to successfully start - default is `60s`
```


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

### bitcoin-regtest
A module containing spring boot starters for integration and regression testing your own application.
It includes functionality to create and fund addresses, send transactions, mine blocks and much more.

### incubator
This subproject is home to all almost-ready modules.

#### tbk-electrum-daemon-client
A module containing a spring boot starter for an [Electrum daemon](https://github.com/moquette-io/moquette) JSON-RPC API client.
It can be used in combination with [spring-testcontainer-electrum-daemon-starter](#spring-testcontainer)!

#### spring-mqtt
A module containing a spring boot starter for [Moquette](https://github.com/moquette-io/moquette) MQTT broker.

#### bitcoin-mqtt
A module containing a spring boot starter for a broker publishing Bitcoin ZeroMQ messages via MQTT.

#### spring-lnurl
The [spring-lnurl module](incubator/spring-lnurl) contains classes and spring security configurations for authentication with [lnurl-auth](https://github.com/fiatjaf/lnurl-rfc).

## Examples
Besides, that most starter modules also have their own example applications, there are also stand-alone 
example applications showing basic usage of the functionality provided by these modules.

- [x] bitcoin-autodca: [Stacking Sats on Kraken: Auto DCA example application](examples/bitcoin-autodca-example-application)
- [x] bitcoin-exchange-rate: [Currency Conversion API example application](examples/bitcoin-exchange-rate-example-application)
- [x] lnd-playground: [Lightning Network Playground example application](examples/lnd-playground-example-application) (using lnd)
- [x] lnurl-auth: [Spring Security Authentication With lnurl-auth example application](examples/incubator/lnurl-auth-example-application)

Example apps can be started with a single command, e.g.:
```shell script
./gradlew -p examples/lnd-playground-example-application bootRun
```


## Development

### Requirements
- java >=11
- docker

A Bitcoin Core Testcontainer running in regtest mode is started for most examples. 
Having access to a Bitcoin Core node running on mainnet is quite useful if you want to try everything.
Optional: A node should publish `rawtx` and `rawblock` messages via zmq for some features to work.

### Build
```shell script
./gradlew build -x test
```
 
### Test
```shell script
./gradlew test integrationTest
```

Tests in example application modules or modules that start a lot of docker containers 
(modules named "*-example-application" or "spring-testcontainer-*") are excluded from the
default test phase and must be manually enabled if you want to run them.
To run all tests pass arguments `-PexampleTest` and `-PtestcontainerTest`:
```shell script
./gradlew test integrationTest -PtestcontainerTest -PexampleTest
```
Be aware this might take several minutes to complete (>= 10 minutes).


### Dependency Verification
Gradle is used for checksum and signature verification of dependencies.

```shell script
# write metadata for dependency verification
./gradlew --write-verification-metadata pgp,sha256 --export-keys
```

See [Gradle Userguide: Verifying dependencies](https://docs.gradle.org/current/userguide/dependency_verification.html)
for more information.


### Checkstyle
[Checkstyle](https://github.com/checkstyle/checkstyle) with adapted [google_checks](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)
is used for checking Java source code for adherence to a Code Standard.

```shell script
# check for code standard violations with checkstyle
./gradlew checkstyleMain
```


### SpotBugs
[SpotBugs](https://spotbugs.github.io/) is used for static code analysis.

```shell script
# invoke static code analysis with spotbugs
./gradlew spotbugsMain
```


## Contributing
All contributions and ideas are always welcome. For any question, bug or feature request, 
please create an [issue](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/issues). 
Before you start, please read the [contributing guidelines](contributing.md).


## Resources

- Bitcoin: https://bitcoin.org/en/getting-started
- Lightning Network: https://lightning.network/
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
---
- Bitcoin Core (GitHub): https://github.com/bitcoin/bitcoin
- lnd (GitHub): https://github.com/LightningNetwork/lnd
- lnurl (GitHub): https://github.com/fiatjaf/lnurl-rfc
- ElectrumX Server (GitHub): https://github.com/spesmilo/electrumx
- Electrum Personal Server (GitHub): https://github.com/chris-belcher/electrum-personal-server
- Electrum Client (GitHub): https://github.com/spesmilo/electrum
- XChange (GitHub): https://github.com/knowm/XChange
- bitcoinj (GitHub): https://github.com/bitcoinj/bitcoinj
- Lightningj (GitHub): https://github.com/lightningj-org/lightningj
- ConsensusJ (GitHub): https://github.com/ConsensusJ/consensusj
- JeroMq (GitHub): https://github.com/zeromq/jeromq
- Project Reactor (GitHub): https://github.com/reactor/reactor-core
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules
- Testcontainers (GitHub): https://github.com/testcontainers/testcontainers-java/
- Tor: https://www.torproject.org/
- Protocol Buffers: https://developers.google.com/protocol-buffers


## License

The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
