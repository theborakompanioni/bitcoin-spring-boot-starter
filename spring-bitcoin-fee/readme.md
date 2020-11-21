
// TODO: move fee providers perform very poorly under small mempool conditions..
// verify all results and adjust the finetuning parameters accordingly.
// e.g.: make min/medium/max durations configurable per fee provider -> autoconfig + properties!
See fee distribution: https://jochen-hoenicke.de/queue/#0,2h


- Credit: The following list has been produced by [0xB10C](https://github.com/0xb10c).
- Original: https://b10c.me/blog/003-a-list-of-public-bitcoin-feerate-estimation-apis/
- License: [Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/).
- Changes: Removed some text. Focus on links, authentication, examples and unit of measurement. Add "last checked" date.
 
- [x] Bitcoin Core JSON-RPC Api - estimatestmartfee
- [x] bitcoiner.live API
- [x] Bitgo API
- [x] Bitcore API
- [x] Blockchain.info API (deprecated - will be removed as it is not compatible with "block target" recommendations)
- [x] Blockchair API
- [x] BlockCypher API
- [x] Blockstream.info API
- [x] BTC.com API
- [x] earn.com API

Incubating: 
- [ ] https://btcpriceequivalent.com/fee-estimates -> https://btcpriceequivalent.com/n/{n}
- [ ] whatthefee.io
- [ ] https://mempool.space/api/v1/fees/recommended

# Resources
- https://b10c.me/blog/003-a-list-of-public-bitcoin-feerate-estimation-apis/


### bitcoiner.live API
- [`https://bitcoiner.live/api/fees/estimates/latest`](https://bitcoiner.live/api/fees/estimates/latest)
- Unit of measurement: **`sat/vByte`**
- Last check: 2020-11-15
- License: Attribution-NonCommercial-ShareAlike 4.0 International License.
- Note: It is free for non-commercial use. If you'd like a commercial license or if you anticipate to make a lot of requests, please just get in touch before!

```json
{
  "timestamp": 1563456789,
  "estimates": {
    "30": {
      "sat_per_vbyte": 12.0,
      [ ... ]
    },
    "60": {
      "sat_per_vbyte": 12.0,
      [ ... ]
    },
    "120": {
      "sat_per_vbyte": 8.0,
      [ ... ]
    },
    [ ... ]
  }
}
```



### Bitgo API
- [`https://www.bitgo.com/api/v2/btc/tx/fee`](https://www.bitgo.com/api/v2/btc/tx/fee)
- [documentation](https://bitgo.com/api/v2/#operation/v2.tx.getfeeestimate)
- Unit of measurement: **`sat/kB`**.  <small> (*`sat/kB / 1000 = sat/Byte`*) </small>
- Last check: 2020-11-15

```json
{
  "feePerKb": 61834,
  "cpfpFeePerKb": 61834,
  "numBlocks": 2,
  "confidence": 80,
  "multiplier": 1,
  "feeByBlockTarget": {
    "1": 64246,
    "2": 61834,
    "3": 56258,
    [ ... ]
  }
}
```



### Bitcore API
- [`https://api.bitcore.io/api/BTC/mainnet/fee/2`](https://api.bitcore.io/api/BTC/mainnet/fee/2)
- https://github.com/bitpay/bitcore/blob/master/packages/bitcore-node/src/routes/api/fee.ts
- Last check: 2020-11-14
- Unit of measurement: **`BTC/kB`**. <small>  *(`BTC/kB x 100000 = sat/Byte`)* </small> 

```json
{
  "feerate": 0.00062164,
  "blocks": 2
}
```



### Blockchain.info API
- [`161616`](https://api.blockchain.info/mempool/fees)
- Last check: 2020-11-13
- Unit of measurement: **`sat/Byte`**

```json
{
  "limits": {
    "min": 2,
    "max": 79
  },
  "regular": 4,
  "priority": 53
}
```



### Blockchair API
- [`https://api.blockchair.com/bitcoin/stats`](https://api.blockchair.com/bitcoin/stats)
- [documentation](https://github.com/Blockchair/Blockchair.Support/blob/master/API.md)
- [documentation](https://blockchair.com/api/docs)
- Last check: 2020-11-14
- Unit of measurement: **`sat/Byte`**
- Pricing: 
  - free for personal or testing/non-commercial or academic of up to 1440 requests/day
  - Commercial plans available

```json
{
  "data": {
    [ ... ]
    "suggested_transaction_fee_per_byte_sat": 1
  },
  [ ... ]
}
```



### BlockCypher API
- [`https://api.blockcypher.com/v1/btc/main`](https://api.blockcypher.com/v1/btc/main)
- [documentation](https://www.blockcypher.com/dev/bitcoin/#restful-resources)
- Last check: 2020-11-14
- Unit of measurement: **`sat/kB`**. <small> (*`sat/kB / 1000 = sat/Byte`*) </small>


```json
{
  [ ... ]
  "high_fee_per_kb": 41770,
  "medium_fee_per_kb": 25000,
  "low_fee_per_kb": 15000,
  [ ... ]
}
```



### Blockstream.info API
- [`https://blockstream.info/api/fee-estimates`](https://blockstream.info/api/fee-estimates)
- [documentation](https://github.com/Blockstream/esplora/blob/master/API.md#fee-estimates)
- Last check: 2020-11-14
- Unit of measurement: **`sat/vByte`** 

```json
{
  "2": 32.749,
  "3": 32.749,
  "4": 24.457,
  "6": 20.098,
  "10": 18.17,
  "20": 10.113,
  "144": 1,
  "504": 1,
  "1008": 1
}
```



### BTC.com API
- [`https://btc.com/service/fees/distribution`](https://btc.com/service/fees/distribution)
- Last check: 2020-11-14
- Unit of measurement: **`sat/Byte`**


```json
{
  "tx_size": [ ... ],
  "tx_size_count": [ ... ],
  "tx_size_divide_max_size": [ ... ],
  "tx_duration_time_rate": [ ... ],
  "fees_recommended": {
    "one_block_fee": 14
  },
  "update_time": "1563456789"
}
```



### earn.com API
- [`https://bitcoinfees.earn.com/api/v1/fees/recommended`](https://bitcoinfees.earn.com/api/v1/fees/recommended)
- [`https://bitcoinfees.earn.com/api/v1/fees/list`](https://bitcoinfees.earn.com/api/v1/fees/list)
- [documentation](https://bitcoinfees.earn.com/api)
- Last check: 2020-11-13
- Unit of measurement: **`sat/Byte`**

`GET /api/v1/fees/recommended`:
```json
{
  "fastestFee": 44,
  "halfHourFee": 44,
  "hourFee": 4
}
```

`GET /api/v1/fees/list`:
```json
{ "fees": [ 
  {"minFee":0,"maxFee":0,"dayCount":545,"memCount":87,
  "minDelay":4,"maxDelay":32,"minMinutes":20,"maxMinutes":420},
  [...]
 ] }
```


All text and images in this readme.md are licensed under a [Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/).
