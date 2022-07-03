# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- disable p2p network activity by default for bitcoin testcontainer (`networkactive=0`)

### Changed
- upgrade: update spring-boot from v2.5.6 to v2.7.1
- upgrade: update bitcoin testcontainer from v0.21.1 to v23
- upgrade: update electrumx testcontainer from v1.15.0 to v1.16.0
- upgrade: update electrum-personal-server testcontainer from v0.2.1.1 to v0.2.2
- upgrade: update bitcoin-kmp from v0.8.1 to v0.8.2
- upgrade: update bitcoinj from v0.15.10 to v0.16.1
- upgrade: update jmolecules bom from v2021.1.0 to v2021.2.0
- upgrade: update testcontainers from v1.16.2 to v1.17.3
- upgrade: update bitcoin-kmp from v0.8.2 to v0.8.4
- upgrade: update sqlite v3.34.0 to v3.36.0.3
- upgrade: update protobuf-java v3.19.1 to v3.21.2
- upgrade: update protoc-gen-grpc-java v1.41.1 to v1.47.0

### Breaking
- upgrade: update consensusj from v0.5.9 to 0.5.14

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

[Unreleased]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.3.0...HEAD
[0.3.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/theborakompanioni/bitcoin-spring-boot-starter/releases/tag/0.1.0

