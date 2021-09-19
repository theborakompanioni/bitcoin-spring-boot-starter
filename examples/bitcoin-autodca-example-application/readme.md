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

```yaml
org.tbk.bitcoin.autodca:
  fiat-currency: 'USD' # the governmental shitcoin you are selling e.g. 'USD', 'EUR', etc.
  fiat-amount: '21.00' # fiat amount you trade for the future of money
  max-relative-fee: 0.5 # maximum fee in % that you are willing to pay e.g. 0.5 (in percent)
  withdraw-address: 'bc1yourwithdrawaladdress' # your withdrawal address
```

```yaml
org.tbk.xchange:
  enabled: true
  specifications:
    krakenExchange:
      exchange-class: org.knowm.xchange.kraken.KrakenExchange
      api-key: 'your-api-key' # change this value to your api key
      secret-key: 'your-secret-key' #  change this value to your secret key
```

# Run
### `help`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--help'
```

### `stack`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry stack'
```

### `withdraw`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry withdraw'
```

### `balance`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry balance'
```
```
2009-01-03 15:38:25.761 DEBUG 34924 --- [  restartedMain] o.t.b.a.e.c.KrakenBalanceCommandRunner   : Fetch balance on exchange Kraken#250590866
💰  ZUSD:       42.1337
💰  XXBT:       0.00312009
```

### `history`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry history'
```
```
💸 STUVWY-YZ123-456789: closed          buy       0.00001314    (100.0%)        @ limit          133700.2        XBTUSD
💸 ABCDEF-GHIJK-LMOPQR: closed          buy       0.00004245    (100.0%)        @ limit           42000.1        XBTUSD
```

### `rebalance`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry rebalance'
```
```
2009-01-03 15:45:33.405 DEBUG 35450 --- [  restartedMain] o.t.b.a.e.c.KrakenRebalanceCommandRunner : Calculate rebalance on exchange Kraken#1392861449
📎 1 BTC = 13504.00148 USD
💰 Balance BTC (49.00%): BTC 0.00305768 (41.29 USD)
💰 Balance FIAT (51.00%): USD 42.9700 (0.00318202 BTC)
📎 Target Balance BTC (50.0%): 0.00312009 BTC
📈 You should use USD 1.00 to get BTC 0.00006241
```

### `ticker`
```shell script
./gradlew -p examples/bitcoin-autodca-example-application bootRun --args='--dry ticker'
```
```
2009-01-03 16:18:54.959 DEBUG 75022 --- [  restartedMain] o.t.b.a.e.command.TickerCommandRunner    : Fetch ticker on exchange Kraken#951362619
Name                   Value    Instrument          Spread 
📈 Ask:                42.70       BTC/USD
📉 Bid:                42.60       BTC/USD         0.00468%
- High:                44.40       BTC/USD        -1.97653%
- Low:                 41.00       BTC/USD         0.87304%
- Open:                41.10       BTC/USD        -1.45138%
- Last:                42.60       BTC/USD         0.00468%
```

# Resources
- https://github.com/dennisreimann/stacking-sats-kraken