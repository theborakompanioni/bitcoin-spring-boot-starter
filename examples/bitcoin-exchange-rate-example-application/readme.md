bitcoin-exchange-rate-example-application
===

Currency Conversion API example application.


Start the application with
```shell
./gradlew -p examples/bitcoin-exchange-rate-example-application bootRun
```

... and open the following url in your browser: 
http://localhost:8080/swagger-ui/index.html

### Example API request
Request: `http://localhost:8080/api/v1/exchange/latest?base=BTC&target=USD&provider=KRAKEN`

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


## API

`GET /api/v1/exchange`:
```json
{
  "providerChain" : [ "IDENT", "KRAKEN", "BITSTAMP", "BITTREX", "BITFINEX", "GEMINI", "THEROCK", "BITCOIN-STANDARD" ],
  "providerNames" : [ "ECB-HIST90", "IMF", "ECB-HIST", "ECB", "KRAKEN", "IMF-HIST", "BITTREX", "GEMINI", "THEROCK", "BITSTAMP", "IDENT", "BITFINEX", "BITCOIN-STANDARD" ]
}
```

`GET /api/v1/exchange/latest?base=BTC&target=USD`:
```json
{
  "base" : "BTC",
  "rates" : [ {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "13225.00000",
    "meta" : { },
    "provider" : "KRAKEN",
    "target" : "USD",
    "type" : "DEFERRED"
  }, {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "13225.12",
    "meta" : { },
    "provider" : "BITSTAMP",
    "target" : "USD",
    "type" : "DEFERRED"
  }, {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "13231.98800000",
    "meta" : { },
    "provider" : "BITTREX",
    "target" : "USD",
    "type" : "DEFERRED"
  }, {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "13219",
    "meta" : { },
    "provider" : "BITFINEX",
    "target" : "USD",
    "type" : "DEFERRED"
  }, {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "13230.00",
    "meta" : { },
    "provider" : "GEMINI",
    "target" : "USD",
    "type" : "DEFERRED"
  }, {
    "base" : "BTC",
    "chain" : [ ],
    "derived" : false,
    "factor" : "14890.0",
    "meta" : { },
    "provider" : "THEROCK",
    "target" : "USD",
    "type" : "DEFERRED"
  } ]
}
```


`GET /api/v1/exchange/latest?base=EUR&target=USD&provider=BITCOIN-STANDARD`:
```json
{
  "base" : "EUR",
  "rates" : [ {
    "base" : "EUR",
    "chain" : [ {
      "base" : "BTC",
      "chain" : [ ],
      "derived" : false,
      "factor" : "13228.60000",
      "meta" : { },
      "provider" : "KRAKEN",
      "target" : "USD",
      "type" : "DEFERRED"
    }, {
      "base" : "BTC",
      "chain" : [ ],
      "derived" : false,
      "factor" : "11188.80000",
      "meta" : { },
      "provider" : "KRAKEN",
      "target" : "EUR",
      "type" : "DEFERRED"
    } ],
    "derived" : true,
    "factor" : "1.182307307307307",
    "meta" : { },
    "provider" : "BITCOIN-STANDARD",
    "target" : "USD",
    "type" : "DEFERRED"
  } ]
}
```

# Resources
- JSR354 (GitHub): https://github.com/JavaMoney/jsr354-api
- XChange (GitHub): https://github.com/knowm/XChange