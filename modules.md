bitcoin-spring-boot-starter Modules
===

[See readme.md for general information](readme.md) about the project.

## Modules

### bitcoin-jsonrpc-client
A module containing a spring boot starter for a [ConsensusJ](https://github.com/ConsensusJ/consensusj) Bitcoin Core JSON-RPC API client.
The starter will automatically create an injectable `BitcoinClient` bean:

```yaml
org.tbk.bitcoin.jsonrpc:
  enabled: true # whether auto-config should run - default is `true`
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
  enabled: true # whether auto-config should run - default is `true`
  network: mainnet
  zmqpubrawblock: tcp://localhost:28332
  zmqpubrawtx: tcp://localhost:28333
  zmqpubhashblock: tcp://localhost:28334
  zmqpubhashtx: tcp://localhost:28335
```

Also, if you have [bitcoinj](https://github.com/bitcoinj/bitcoinj) in the classpath, it will create a bean
of type `BitcoinjTransactionPublisherFactory` and `BitcoinjBlockPublisherFactory` which will emit `bitcoinj` types for your convenience.


### cln-grpc-client
A module containing a spring boot starter for a CLN gRPC API client.
The starter will automatically create injectable `NodeStub`, `NodeFutureStub` and `NodeBlockingStub` beans:

```yaml
org.tbk.lightning.cln.grpc:
  enabled: true # whether auto-config should run - default is `true`
  host: localhost
  port: 19935
  ca-cert-file-path: '/home/user/.lightning/regtest/ca.pem'
  client-cert-file-path: '/home/user/.lightning/regtest/client.pem'
  client-key-file-path: '/home/user/.lightning/regtest/client-key.pem'
```

or

```yaml
org.tbk.lightning.cln.grpc:
  host: localhost
  port: 19935
  ca-cert-base64: '...yv66vg=='
  client-cert-base64: '...yv66vg=='
  client-key-base64: '...yv66vg=='
```


### lnd-grpc-client
A module containing a spring boot starter for a [LightningJ](https://www.lightningj.org/) LND gRPC API client.
The starter will automatically create injectable `AsynchronousLndAPI` and `SynchronousLndAPI` beans:

```yaml
org.tbk.lightning.lnd.grpc:
  enabled: true # whether auto-config should run - default is `true`
  host: localhost
  port: 10009
  macaroon-file-path: '/home/user/.lnd/data/chain/bitcoin/regtest/admin.macaroon'
  cert-file-path: '/home/user/.lnd/tls.cert'
```

or

```yaml
org.tbk.lightning.lnd.grpc:
  host: localhost
  port: 10009
  macaroon-base64: '...yv66vg=='
  cert-base64: '...yv66vg=='
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
Moved to a distinct repo: https://github.com/theborakompanioni/tor-spring-boot-starter


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
- [x] [Bitcoin Core](https://github.com/bitcoin/bitcoin) with `spring-testcontainer-bitcoind-starter`
- [x] [cln](https://github.com/ElementsProject/lightning) with `spring-testcontainer-cln-starter`
- [x] [lnd](https://github.com/LightningNetwork/lnd) with `spring-testcontainer-lnd-starter`
- [x] [ElectrumX](https://github.com/spesmilo/electrumx) with `spring-testcontainer-electrumx-starter`
- [x] [Electrum Personal Server](https://github.com/chris-belcher/electrum-personal-server) with `spring-testcontainer-electrum-personal-server-starter`
- [x] [Electrum](https://github.com/spesmilo/electrum) with `spring-testcontainer-electrum-daemon-starter`
- [x] [Tor](https://www.torproject.org/) with [`spring-testcontainer-tor-starter`](spring-testcontainer/spring-testcontainer-tor-starter)

Most of these spring boot starter modules contain a simple example application. 
They can be used in combination with other modules like [bitcoin-jsonrpc-client](#bitcoin-jsonrpc-client), 
[bitcoin-zeromq-client](#bitcoin-zeromq-client), [lnd-grpc-client](#lnd-grpc-client), etc.


### bitcoin-regtest
A module containing spring boot starters for integration and regression testing your own application.
It includes functionality to create and fund addresses, send transactions, mine blocks and much more.


### incubator
This subproject is home to all almost-ready modules.

#### tbk-electrum-daemon-client
A module containing a spring boot starter for an [Electrum daemon](https://github.com/spesmilo/electrum) JSON-RPC API client.
It can be used in combination with [spring-testcontainer-electrum-daemon-starter](#spring-testcontainer)!

#### spring-lnurl
The [spring-lnurl module](incubator/spring-lnurl) contains classes and spring security configurations for authentication with [lnurl-auth](https://github.com/fiatjaf/lnurl-rfc).
