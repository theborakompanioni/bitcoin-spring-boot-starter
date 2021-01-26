bitcoin-autodca-example-application
===

Stacking Sats on Kraken: Auto DCA example application.

# Prerequisites
You'll need..
- a Kraken account
- a balance with `amount > 0` in a fiat currency
- an api key allowed creating orders and withdrawing funds

# Configure
Adapt the values in `application.yml` to your needs.
Especially the exchange and autodca properties!

```yml
org.tbk.bitcoin.autodca:
  fiat-currency: 'USD' # the governmental shitcoin you are selling e.g. 'USD', 'EUR', etc.
  fiat-amount: '21.00' # fiat amount you trade for the future of money
  max-relative-fee: 0.5 # maximum fee in % that you are willing to pay e.g. 0.5 (in percent)
  withdraw-address: 'bc1yourwithdrawaladdress' # your withdrawal address
```

```yml
org.tbk.xchange:
  enabled: true
  specifications:
    krakenExchange:
      exchange-class: org.knowm.xchange.kraken.KrakenExchange
      api-key: 'your-api-key' # change this value to your api key
      secret-key: 'your-secret-key' #  change this value to your secret key
```

# Run
```shell script
./gradlew -p examples/incubator/bitcoin-autodca-example-application bootRun --args='--help'
```

```shell script
./gradlew -p examples/incubator/bitcoin-autodca-example-application bootRun --args='--dry stack'
```
```shell script
./gradlew -p examples/incubator/bitcoin-autodca-example-application bootRun --args='--dry withdraw'
```
```shell script
./gradlew -p examples/incubator/bitcoin-autodca-example-application bootRun --args='--dry history'
```

# Resources
- https://github.com/dennisreimann/stacking-sats-kraken