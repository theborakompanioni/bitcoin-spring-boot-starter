Example application for monetary conversion

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
