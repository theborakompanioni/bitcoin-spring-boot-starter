[![Build Status](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter.svg?branch=master)](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter)
[![Download](https://jitpack.io/v/theborakompanioni/spring-boot-bitcoin-starter.svg)](https://jitpack.io/#theborakompanioni/spring-boot-bitcoin-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/spring-boot-bitcoin-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE)

spring-boot-bitcoin-starter
===

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

#### spring-testcontainer-bitcoind-starter
Start and run [Bitcoin Core](https://github.com/bitcoin/bitcoin) daemons.

This spring boot starter module can be used in combination with
[bitcoin-jsonrpc-client](#bitcoin-jsonrpc-client) and [bitcoin-zeromq-client](#bitcoin-zeromq-client).

#### spring-testcontainer-lnd-starter
Start and run [lnd](https://github.com/LightningNetwork/lnd) daemons.

Currently, it **can only** be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

#### spring-testcontainer-electrumx-starter
Start and run [ElectrumX](https://github.com/spesmilo/electrumx) instances.

It can be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

#### spring-testcontainer-electrum-personal-server-starter
Start and run [Electrum Personal Server](https://github.com/chris-belcher/electrum-personal-server) instances.

It can be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

#### spring-testcontainer-electrum-daemon-starter
Start and run [Electrum](https://github.com/spesmilo/electrum) daemons. 

It can be used in combination with [spring-testcontainer-electrumx-starter](#spring-testcontainer-electrumx-starter)
and [spring-testcontainer-electrum-personal-server-starter](#spring-testcontainer-electrum-personal-server-starter)!

#### spring-testcontainer-tor-starter
Start and run [Tor](https://www.torproject.org/) daemons. 
See [spring-testcontainer-tor-starter](spring-testcontainer/spring-testcontainer-tor-starter) or the
[example application](spring-testcontainer/spring-testcontainer-tor-example-application) for more information!


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
This module is home to all almost-ready packages.

#### tbk-tor
A module containing a spring boot starter for an embedded [Tor daemon](https://www.torproject.org/).
The starter will automatically expose your application as hidden service!
Easily create hidden service sockets programmatically within your Spring Boot application.

A common configuration can look like this:
```yaml
org.tbk.tor:
  enabled: true  # whether auto-config should run - default is `true`
  auto-publish-enabled: true # auto publish the web port as hidden service - default is `true`
  working-directory: 'my-tor-directory' # the working directory for tor - default is `tor-working-dir`
  startup-timeout: 30s # max startup duration for tor to successfully start - default is `60s`
```

Start the example application with
```shell
./gradlew -p incubator/tbk-tor/tbk-tor-example-application bootRun
```
Example output (2021-01-21):
```
2021-01-21 01:23:30.035  INFO 313251 --- [  restartedMain] org.berndpruenster.netlayer.tor.Tor      : Starting Tor
2021-01-21 01:23:33.490  INFO 313251 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-01-21 01:23:33.511  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : Started TorExampleApplication in 8.417 seconds (JVM running for 8.972)
2021-01-21 01:23:33.605  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : =================================================
2021-01-21 01:23:33.606  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : url: http://<your_onion_url>.onion:80
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : virtual host: <your_onion_url>.onion
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : virtual port: 80
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : host: 127.0.0.1
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : port: 8080
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : directory: /home/tbk/workspace/spring-boot-bitcoin-starter/incubator/tbk-tor/tbk-tor-example-application/tor-working-dir/spring_boot_app
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : -------------------------------------------------
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : run: torsocks -p 46735 curl http://<your_onion_url>.onion:80/index.html -v
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : =================================================
```

#### tbk-electrum-daemon-client
A module containing a spring boot starter for a [Electrum daemon](https://github.com/spesmilo/electrum) JSON-RPC API client.
It can be used in combination with  [spring-testcontainer-electrum-daemon-starter](#spring-testcontainer-electrum-daemon-starter)!


### examples
Besides, that most starter modules also have their own example applications, there are also stand-alone 
example applications showing basic usage of the functionality provided by these modules.

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
2021-01-21 01:01:33.306  INFO 309543 --- [  restartedMain] .l.l.p.e.LndPlaygroundExampleApplication : Started LndPlaygroundExampleApplication in 18.561 seconds (JVM running for 19.232)
2021-01-21 01:01:33.948  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:33.948  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] identity_pubkey: 03d2db919c802c7b69b24be758db10c0d7347aefab525e8f00e41a87904631eaec
2021-01-21 01:01:33.949  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] alias: tbk-lnd-example-application
2021-01-21 01:01:33.949  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] version: 0.11.1-beta commit=v0.11.1-beta-4-g3c4471f8818a07e63864d39a1c3352ce19e8f31d
2021-01-21 01:01:39.275 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Trying to mine one block with coinbase reward for address bcrt1qn2r960p8ykxv4ltxlmp7cxcercpqpteplzczwd
2021-01-21 01:01:39.292 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Mined 1 blocks with coinbase reward for address bcrt1qn2r960p8ykxv4ltxlmp7cxcercpqpteplzczwd
2021-01-21 01:01:39.293 DEBUG 309543 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT2.309S
2021-01-21 01:01:39.378  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block height: 1
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block hash: 568c2f2d64a38f00d9e394210c38c85e434a881bb5946e9e53955bb748bb2233
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] best header timestamp: 1611187299
2021-01-21 01:01:39.382  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:39.431  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [bitcoind] new best block (height: 1): 568c2f2d64a38f00d9e394210c38c85e434a881bb5946e9e53955bb748bb2233
2021-01-21 01:01:41.307 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Trying to mine one block with coinbase reward for address bcrt1qnjvm38z9pe4u4n5sm3vz7rx5g5rr36x6crxkrn
2021-01-21 01:01:41.314 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Mined 1 blocks with coinbase reward for address bcrt1qnjvm38z9pe4u4n5sm3vz7rx5g5rr36x6crxkrn
2021-01-21 01:01:41.315 DEBUG 309543 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT1.581S
2021-01-21 01:01:41.383  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:41.383  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block height: 2
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block hash: 70160a48c15670c5136a057669bc11a57bf78c85f77e74b9a7c3f29ea8769079
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] best header timestamp: 1611187301
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:41.392  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [bitcoind] new best block (height: 2): 70160a48c15670c5136a057669bc11a57bf78c85f77e74b9a7c3f29ea8769079
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
