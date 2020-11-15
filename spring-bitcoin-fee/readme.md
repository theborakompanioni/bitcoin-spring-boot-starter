


# Resources
- https://b10c.me/blog/003-a-list-of-public-bitcoin-feerate-estimation-apis/


### bitcoiner.live API
- [`https://bitcoiner.live/api/fees/estimates/latest`](https://bitcoiner.live/api/fees/estimates/latest)

Type: **`sat/vByte`**

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

Type: **`sat/kB`**.  <small> (*`sat/kB / 1000 = sat/Byte`*) </small>


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



### Bitpay Insight API
- [`https://insight.bitpay.com/api/utils/estimatefee?nbBlocks=2,4,6`](https://insight.bitpay.com/api/utils/estimatefee?nbBlocks=2,4,6)

Type: **`BTC/kB`**. <small>  *(`BTC/kB x 100000 = sat/Byte`)* </small> 

```json
{
  "2": 0.00051894,
  "4": 0.00047501,
  "6": 0.00043338
}
```



### Blockchain.info API
- [`https://api.blockchain.info/mempool/fees`](https://api.blockchain.info/mempool/fees)

Type: **`sat/Byte`**

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

- Type: **`sat/Byte`**

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

Type: **`sat/kB`**. <small> (*`sat/kB / 1000 = sat/Byte`*) </small>


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

Type: **`sat/vByte`** 

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

Type: **`sat/Byte`**


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

Type: **`sat/Byte`**

```json
{
  "fastestFee": 44,
  "halfHourFee": 44,
  "hourFee": 4
}
```
