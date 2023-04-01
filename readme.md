[![Build](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/release/theborakompanioni/bitcoin-spring-boot-starter.svg?maxAge=3600)](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.theborakompanioni/bitcoin-jsonrpc-client-core.svg?maxAge=3600)](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22)
[![License](https://img.shields.io/github/license/theborakompanioni/bitcoin-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/LICENSE)


<p align="center">
    <img src="https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


bitcoin-spring-boot-starter
===

**Write enterprise Bitcoin applications with Spring Boot.**

Spring boot starter projects with convenient dependency descriptors for multiple Bitcoin related modules that you can 
include in your application. Strong focus on integration and regression testing your own application or module.
Included are features for representing, transporting, and performing comprehensive calculations and tests with 
Bitcoin in financial applications and computations.

**Hint**: Of course you can make use of the libraries even if you are not working with Spring!
 
**Note**: Most code is still experimental - **use with caution**.
This project is under active development. Pull requests and issues are welcome.
[Look at the changelog](changelog.md) to track notable changes.
<a id="fun"></a>Also, [developing this project is fun](docs/FALSEHOODS.md).


## Table of Contents

- [Install](#install)
- [Modules](#modules)
- [Examples](#examples)
- [Development](#development)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)


## Install

[Download](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22) from Maven Central.

### Gradle
```groovy
repositories {
    mavenCentral()
    maven {
        // needed for netlayer packages
        url "https://jitpack.io"
    }
    maven {
        // needed for consensusj
        url "https://gitlab.com/api/v4/projects/8482916/packages/maven"
    }
}
```

```groovy
implementation "io.github.theborakompanioni:bitcoin-jsonrpc-client-starter:${bitcoinSpringBootStarterVersion}"
```

### Maven
```xml
<dependency>
    <groupId>io.github.theborakompanioni</groupId>
    <artifactId>bitcoin-jsonrpc-client-starter</artifactId>
    <version>${bitcoinSpringBootStarter.version}</version>
</dependency>
```

The example above imports module `bitcoin-jsonrpc-client-starter` - you can import any module by its name.


## Modules

[This project contains various modules](readme.md) that can be integrated into your project depending on your requirements.

You can find a small selection in the following table. But there is much more to discover.


<!-- there are external links on the anchor #spring-lnurl - so please do not remove it -->
| Module                                                          | Description                                                                                                                                  |
|-----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| [bitcoin-jsonrpc-client](modules.md#bitcoin-jsonrpc-client)     | Connect to Bitcoin Core via [ConsensusJ](https://github.com/ConsensusJ/consensusj) Bitcoin Core JSON-RPC API client.                         |
| [bitcoin-zeromq-client](modules.md#bitcoin-zeromq-client)       | Connect to Bitcoin Core via zmq.                                                                                                             |
| [bitcoin-regtest](modules.md#bitcoin-regtest)                   | Integration and regression testing your own application.                                                                                     |
| [cln-grpc-client](modules.md#cln-grpc-client)                   | Connect to cln via gRPC.                                                                                                                     |
| [lnd-grpc-client](modules.md#lnd-grpc-client)                   | Connect to lnd via gRPC using [LightningJ](https://www.lightningj.org/).                                                                     |
| [xchange-jsr354](modules.md#xchange-jsr354)                     | Exchange rates from Bitcoin exchanges for your application.                                                                                  |
| [spring-xchange](modules.md#spring-xchange)                     | Automatically create and configure [XChange](https://github.com/knowm/XChange) beans. <br />Remember to **get your coins off of exchanges**! |
| [spring-tor](modules.md#spring-tor)                             | Automatically expose your application as [Tor Hidden Service](https://www.torproject.org/).                                                  |
| <a id="spring-lnurl"></a>[spring-lnurl](incubator/spring-lnurl) | Spring Security configurations for authentication with [lnurl-auth](https://github.com/fiatjaf/lnurl-rfc).                                   |

[See modules.md for general information](modules.md) about specific modules.


## Examples
Besides, that most starter modules also have their own example applications, there are also stand-alone 
example applications showing basic usage of the functionality provided by these modules.

- [x] bitcoin-autodca: [Stacking Sats on Kraken: Auto DCA example application](examples/bitcoin-autodca-example-application)
- [x] bitcoin-exchange-rate: [Currency Conversion API example application](examples/bitcoin-exchange-rate-example-application)
- [x] ln-playground: [Lightning Network Playground example application](examples/ln-playground-example-application)
- [x] lnurl-auth: [Spring Security Authentication with `lnurl-auth` example application](incubator/spring-lnurl/spring-lnurl-auth-example-application)

Example apps can be started with a single command, e.g.:
```shell script
./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun
```


## Development

### Requirements
- java >=17
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
./gradlew test integrationTest --rerun-tasks
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
./gradlew checkstyleMain --rerun-tasks
```

### SpotBugs
[SpotBugs](https://spotbugs.github.io/) is used for static code analysis.

```shell script
# invoke static code analysis with spotbugs
./gradlew spotbugsMain --rerun-tasks
```


## Contributing
All contributions and ideas are always welcome. For any question, bug or feature request, 
please create an [issue](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/issues). 
Before you start, please read the [contributing guidelines](contributing.md).


## Resources

- Bitcoin: https://bitcoin.org/en/getting-started
- Lightning Network: https://lightning.network
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
- Testcontainers (GitHub): https://github.com/testcontainers/testcontainers-java
- Tor: https://www.torproject.org
---
- Bitcoin Core (GitHub): https://github.com/bitcoin/bitcoin ([Docker](https://hub.docker.com/r/ruimarinho/bitcoin-core))
- bitcoin-kmp (GitHub): https://github.com/ACINQ/bitcoin-kmp
- bitcoinj (GitHub): https://github.com/bitcoinj/bitcoinj
- cln (GitHub): https://github.com/ElementsProject/lightning ([Docker](https://hub.docker.com/r/elementsproject/lightningd))
- lnd (GitHub): https://github.com/LightningNetwork/lnd ([Docker](https://hub.docker.com/r/lightninglabs/lnd))
- lnurl (GitHub): https://github.com/fiatjaf/lnurl-rfc
- LightningJ (GitHub): https://github.com/lightningj-org/lightningj
- lightning-kmp (GitHub): https://github.com/ACINQ/lightning-kmp
- ElectrumX Server (GitHub): https://github.com/spesmilo/electrumx ([Docker](https://hub.docker.com/r/lukechilds/electrumx))
- Electrum Personal Server (GitHub): https://github.com/chris-belcher/electrum-personal-server ([Docker](https://hub.docker.com/r/btcpayserver/eps))
- Electrum Client (GitHub): https://github.com/spesmilo/electrum ([Docker](https://hub.docker.com/r/osminogin/electrum-daemon))
- ConsensusJ (GitHub): https://github.com/ConsensusJ/consensusj
- JeroMq (GitHub): https://github.com/zeromq/jeromq
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules
- springdoc-openapi (GitHub): https://github.com/springdoc/springdoc-openapi
- sqlite (GitHub): https://github.com/xerial/sqlite-jdbc
- Project Reactor (GitHub): https://github.com/reactor/reactor-core
- Protocol Buffers: https://developers.google.com/protocol-buffers
- XChange (GitHub): https://github.com/knowm/XChange


## License

The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
