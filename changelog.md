# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Breaking
- remove deprecated methods from `LnurlAuthWalletToken`
- upgrade: update lightning-kmp from v1.5.15 to v1.6.2

### Added
- import lnurl-simple in spring-lnurl-auth-starter
- fee: new module bitcoin-fee-estimate-strike

### Changed
- upgrade: update spring-boot from v3.2.1 to v3.3.0
- upgrade: update secp256k1-kmp from v0.13.0 to v0.15.0
- upgrade: update bitcoin-kmp from v0.16.0 to v0.19.0
- upgrade: update jmolecules from v2023.1.1 to v2023.1.3
- upgrade: update sqlite from v3.45.1.0 to v3.45.3.0
- upgrade: update jeromq from v0.5.4 to v0.6.0
- upgrade: update cln-grpc-client-core from v23.8.1 to v24.5.0
- upgrade: update bitcoin testcontainer from v26 to v27
- upgrade: update cln testcontainer from v23.08 to v24.05
- upgrade: update lnd testcontainer from v0.17.0-beta to v0.18.0-beta
- upgrade: update testcontainers from v1.19.3 to v1.19.8

## [0.13.0] - 2024-02-14

### Breaking
- replace `LnurlAuthPairingService` with `LnurlAuthUserPairingService`

### Fixed
- fix: cyclic dependency issue in spring-lnurl-auth-autoconfigure
  
### Changed
- upgrade: update sqlite from v3.44.1.0 to v3.45.1.0
- upgrade: update hibernate-community-dialects from v6.2.5.Final to v6.4.3.Final
- upgrade: update secp256k1-kmp from v0.12.0 to v0.13.0
- upgrade: update bitcoin-kmp from v0.15.0 to v0.16.0
- upgrade: update lightning-kmp from v1.5.14 to v1.5.15
- upgrade: update guava from v30.1-jre to 33.0.0-jre

## [0.12.0] - 2024-01-19

### Breaking
- move protobuf classes to own package in bitcoin-fee modules

### Changed
- upgrade: update spring-boot from v3.1.4 to v3.2.1
- upgrade: update cln-grpc-client-core from v23.5.2 to v23.8.1
- upgrade: update lightning-kmp from v1.5.6 to v1.5.14
- upgrade: update bitcoin-kmp from v0.13.0 to v0.15.0
- upgrade: update lightningj from v0.16.2-Beta to v0.17.0-Beta
- upgrade: update testcontainers from v1.18.3 to v1.19.1
- upgrade: update jeromq from v0.5.3 to v0.5.4
- upgrade: update lnd testcontainer from v0.16.2-beta to v0.17.0-beta
- upgrade: update cln testcontainer from v23.05 to v23.08
- upgrade: update lombok from v1.18.26 to v1.18.30
- upgrade: update consensusj from v0.6.4 to v0.6.5
- upgrade: update sqlite from v3.42.0.0 to v3.44.1.0
- upgrade: update jmolecules from v2022.3.0 to v2023.1.1

### Fixed
- fix: format sats with space grouping char in BitcoinAmountFormat

## [0.11.0] - 2023-09-21
### Breaking
- module bitcoin-jsonrpc-client-autoconfigure: dependencies not included in anymore
- module cln-grpc-client-autoconfigure: dependencies not included in anymore
- module lnd-grpc-client-autoconfigure: dependencies not included in anymore
- autoconfig property prefix of bitcoin jsonrpc cache changed from "jsonrpc.cache" to "jsonrpc-cache"
- bitcoin-regtest-starter does not import testcontainer dependencies for bitcoin, electrumx and electrum-daemon automatically anymore

### Changed
- improved default specs for bitcoin jsonrpc client caches
- upgrade: update spring-boot from v3.1.0 to v3.1.4
- upgrade: update cln-grpc-client-core from v23.5.1 to v23.5.2
- upgrade: update lightning-kmp from v1.4.4 to v1.5.6
- upgrade: update bitcoin-kmp from v0.12.0 to v0.13.0

## [0.10.0] - 2023-06-27
### Breaking
- change bean methods visibility in autoconfig classes to package-private

### Added
- use spring-boot-docker-compose in bitcoin-regtest example application
- lnd: create beans for ChainKit LightningJ APIs

### Changed
- upgrade: update bitcoin-kmp from v0.11.1 to v0.12.0
- upgrade: update bitcoinj from v0.16.1 to v0.16.2
- upgrade: update lightningj from v0.16.0-Beta to v0.16.2-Beta
- upgrade: update grpc from v1.54.1 to v1.56.0
- upgrade: update protobuf from v3.21.12 to v3.22.3
- upgrade: update hibernate-community-dialects from v6.1.7.Final to v6.2.5.Final
- upgrade: update testcontainers from v1.17.6 to v1.18.3
- upgrade: update sqlite from v3.41.2.1 to v3.42.0.0

### Fixed
- Fix json serialization of FeeRecommendationResponse in bitcoin-fee

### Removed
- module: externalize 'cln-grpc-client-core'

## [0.9.0] - 2023-06-15
### Breaking
- change bitcoin testcontainer image from ruimarinho/bitcoin-core to polarlightning/bitcoind
- change cln testcontainer image from elementsproject/lightningd to polarlightning/clightning
- deprecation: scheduled `rpcport` config property of cln testcontainer for removal

### Added
- lnd-grpc-client: ability to specify cert and macaroon as base64-encoded value
- cln-grpc-client: ability to specify certs and key as base64-encoded value

### Changed
- rebuild `cln-grpc-client` to work with CLN v23.05.1
- upgrade: update jmolecules bom from v2022.2.4 to v2022.3.0
- upgrade: update lnd testcontainer from v0.16.1-beta to v0.16.2-beta
- upgrade: update springdoc-openapi from v2.0.4 to v2.1.0
- upgrade: update spring-tor from v0.7.0 to v0.8.0
- upgrade: update spring-boot from v3.0.6 to v3.1.0
- upgrade: update bitcoin testcontainer from v24 to v25

## [0.8.0] - 2023-04-29
### Changed
- upgrade: update bitcoin testcontainer from v23 to v24
- upgrade: update lnd testcontainer from v0.16.0-beta to v0.16.1-beta
- upgrade: update lightning-kmp from v1.4.1 to v1.4.4
- upgrade: update lombok from v1.18.20 to v1.18.26
- upgrade: update spring-boot from v3.0.4 to v3.0.6
- upgrade: update grpc from v1.50.0 to v1.54.1

### Fixed
- fix: use canonical form for property conditions
- fix: parse all exchange properties in module `spring-xchange`

### Removed
- deprecated class `org.tbk.bitcoin.common.util.Hex`

## [0.7.0] - 2023-04-04
### Breaking
- lnd-grpc-client: rename `rpchost` and `rpcport` properties to `host` and `port`
- upgrade: update docker lightninglabs/lnd from v0.15.5-beta to v0.16.0-beta
- upgrade: update lightningj from v0.15.5-Beta to v0.16.0-Beta

### Added
- module: initial version of module `cln-grpc-client`
- module: initial version of module `spring-testcontainer-cln-starter`
- autoconfiguration for compatibility of grpc clients with lightning testcontainers

### Changed
- deprecation: scheduled `org.tbk.bitcoin.common.util.Hex` for removal
- upgrade: update spring-boot from v3.0.2 to v3.0.4
- upgrade: update sqlite from v3.40.0.0 to v3.41.2.1
- upgrade: update hibernate-community-dialects from v6.1.5.Final to v6.1.7.Final
- upgrade: update bitcoin-kmp from v0.11.0 to v0.11.1
- upgrade: update secp256k1-kmp from v0.7.1 to v0.8.0
- upgrade: update springdoc-openapi from v2.0.2 to v2.0.4
- upgrade: update jmolecules bom from v2022.2.2 to v2022.2.4

## [0.6.0] - 2023-02-23
### Breaking
- upgrade: update spring-boot from v2.7.6 to v3.0.2
- upgrade: update spring-tor from v0.6.0 to v0.7.0
- upgrade: update springdoc-openapi from v1.6.13 to v2.0.2
- 
### Added
- module: initial version of module `bitcoin-zeromq-client-bitcoin-kmp`
- ability to disable library specific zmq auto configuration

### Changed
- upgrade: update bitcoin-kmp from v0.10.0 to v0.11.0
- upgrade: update secp256k1-kmp from v0.7.0 to v0.7.1
- upgrade: update jeromq from v0.5.2 to v0.5.3
- upgrade: update lightningj from v0.15.3-Beta to v0.15.5-Beta
- upgrade: update protobuf-gradle-plugin from v0.8.17 to v0.9.2
- upgrade: update grpc from v1.49.2 to v1.50.0
- upgrade: update protobuf from v3.21.7 to v3.21.12
- upgrade: update testcontainers from v1.17.5 to v1.17.6
- upgrade: update xchange from v5.0.13 to v5.1.0
- upgrade: update docker lightninglabs/lnd from v0.15.3-beta to v0.15.5-beta
- upgrade: update docker getumbrel/btc-rpc-explorer from v2.1.0 to v3.3.0

## [0.5.0] - 2022-11-29
### Breaking
- upgrade: update java from v11 to v17
- lnurl-auth: allow passing target url query parameter

### Added
- ability to apply custom `UserDetailsService` for `LnurlAuthConfigurer`

### Changed
- change lnd docker image from lnzap/lnd to lightninglabs/lnd
- replace springfox v3.0.0 with springdoc-openapi v1.6.13
- upgrade: update lightningj from v0.12.1-Beta to v0.15.3-Beta
- upgrade: update spring-boot from v2.7.3 to v2.7.6
- upgrade: update testcontainers from v1.17.3 to v1.17.5
- upgrade: update grpc from v1.47.0 to v1.49.2
- upgrade: update protobuf from v3.21.2 to v3.21.7
- upgrade: update jmolecules bom from v2021.2.0 to v2022.2.2
- upgrade: update bytebuddy from v1.10.22 to v1.12.19
- upgrade: update checkstyle from v8.44 to v10.3.4
- upgrade: update consensusj from v0.6.3 to v0.6.4
- upgrade: update bitcoin-kmp from v0.8.5 to v0.10.0
- upgrade: update secp256k1-kmp from v0.6.4 to v0.7.0
- upgrade: update gradle from v7.4.2 to v7.5.1
- upgrade: update findsecbugs plugin from v1.11.0 to v1.12.0
- upgrade: update nebula lint plugin from v17.6.1 to v17.7.1
- upgrade: update nebula release plugin from v16.0.0 to v17.1.0
- upgrade: update nebula project plugin from v9.6.3 to v10.0.1
- upgrade: update spring-tor from v0.5.0 to v0.6.0
- upgrade: update sqlite from v3.39.3.0 to v3.40.0.0

### Fixed
- fix: allow non-https localhost lnurls
- fix: lnurl copy button on login page served over tor

### Removed
- module: externalize 'spring-tor'

## [0.4.1] - 2022-10-09
### Changed
- upgrade: update netlayer from v0.7.2 to v0.7.5

## [0.4.0] - 2022-10-02
### Breaking
- upgrade: update spring-boot from v2.5.6 to v2.7.3
- upgrade: update consensusj from v0.5.8 to v0.6.3

### Fixed
- fix: tor auto-publish can be disabled (`org.tbk.tor.auto-publish-enabled`)

### Added
- disable p2p network activity by default for bitcoin testcontainer (`networkactive=0`)

### Changed
- upgrade: update bitcoin testcontainer from v0.21.1 to v23
- upgrade: update bitcoin-kmp from v0.8.1 to v0.8.5
- upgrade: update bitcoinj from v0.15.10 to v0.16.1
- upgrade: update electrumx testcontainer from v1.15.0 to v1.16.0
- upgrade: update electrum-personal-server testcontainer from v0.2.1.1 to v0.2.2
- upgrade: update jmolecules bom from v2021.1.0 to v2021.2.0
- upgrade: update sqlite v3.34.0 to v3.39.3.0
- upgrade: update testcontainers from v1.16.2 to v1.17.3
- upgrade: update protobuf-java v3.19.1 to v3.21.2
- upgrade: update protoc-gen-grpc-java v1.41.1 to v1.47.0
- upgrade: update xchange from v5.0.11 to v5.0.13
- upgrade: update zxing v3.4.1 to v3.5.0

## [0.3.0] - 2021-11-10
### Breaking
- lnurl-auth: enforce compressed (33-byte) secp256k1 public key encoded as hex for `key` param
- build: exclude protobuf files from jar file

### Changed
- build: build javadoc and sources jar only for publication by default
- upgrade: update spring-boot from v2.5.3 to v2.5.6
- upgrade: update netlayer from v0.6.8 to v0.7.2
- upgrade: update jmolecules from v1.2.0 to v1.3.0
- upgrade: update testcontainers from v1.15.2 to v1.16.2
- upgrade: update grpc from v1.38.0 to v1.41.1
- upgrade: update protobuf from v3.17.3 to v3.19.1
- upgrade: switch from acinq/bitcoin-lib v0.19 to acinq/bitcoin-kmp v0.8.1

### Removed
- module: sunset incubator module 'tbk-btcabuse-client'
- module: sunset incubator module 'spring-mqtt'
- module: sunset incubator module 'bitcoin-mqtt'
  
## [0.2.0] - 2021-09-26
### Breaking
- refactor: throw IllegalArgumentException instead of NPE on @NonNull violations
- adapt class names in lnd client module (remove 'JsonRpc' string in names)
- rename lnd info/health actuator property from 'lndJsonRpc' to 'lndApi'
- remove class `LndRpcClientFactory`

### Added
- spring-lnurl: Publish `LnurlAuthWalletActionEvent` on successfully authorized wallet requests
- lnd: create beans for all available LightningJ APIs

### Changed
- spring-lnurl: Improved customization support for `LnurlAuthConfigurer`
- upgrade: update jmolecules from v2021.0.2 to v2021.1.0
- upgrade: update consensusj from v0.5.8 to v0.5.9
- upgrade: update xchange from v5.0.7 to v5.0.11

## [0.1.0] - 2021-08-14
### Added
- Initial release

[Unreleased]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.13.0...HEAD
[0.13.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.12.0...0.13.0
[0.12.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.11.0...0.12.0
[0.11.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.10.0...0.11.0
[0.10.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.9.0...0.10.0
[0.9.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.8.0...0.9.0
[0.8.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.7.0...0.8.0
[0.7.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.6.0...0.7.0
[0.6.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.5.0...0.6.0
[0.5.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.4.1...0.5.0
[0.4.1]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.4.0...0.4.1
[0.4.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/releases/tag/0.1.0
