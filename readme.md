[![Build Status](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter.svg?branch=master)](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/spring-boot-bitcoin-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE)

spring-boot-bitcoin-starter
===

Spring boot starter projects with convenient dependency descriptors for multiple Bitcoin related modules that you can 
include in your application. Strong focus on [JSR354](https://github.com/JavaMoney/jsr354-api) and representing, 
transporting, and performing comprehensive calculations with Bitcoin in financial contexts and monetary computations.

## modules
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

### bitcoin-jsonrpc-client
A module containing a spring boot starter for a [ConsensusJ](https://github.com/ConsensusJ/consensusj) Bitcoin Core JSON-RPC API client.
The Starter will automatically create an autowireable `BitcoinClient` bean:

```yaml
org.tbk.bitcoin:
  enabled: true
  client:
    enabled: true
    network: mainnet
    rpchost: http://localhost
    rpcport: 8332
    rpcuser: myrpcuser
    rpcpassword: 'myrpcpassword'
```

## build
```
./gradlew clean build
```


# Resources
- Bitcoin: https://bitcoin.org/en/getting-started
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
- XChange (GitHub): https://github.com/knowm/XChange
- ConsensusJ (GitHub): https://github.com/ConsensusJ/consensusj

# License
The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
