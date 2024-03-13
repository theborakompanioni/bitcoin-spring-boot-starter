
```shell
curl -L -X GET https://www.bitgo.com/api/v2/btc/tx/fee?numBlocks=10
```
```json
{
  "feePerKb": 19000,
  "cpfpFeePerKb": 19000,
  "numBlocks": 10,
  "feeByBlockTarget": {
    "1": 30012,
    "2": 30012,
    "3": 25883,
    "4": 25500,
    "5": 25500,
    "6": 21019,
    "10": 19054,
    "20": 19021,
    "50": 17500,
    "100": 17500,
    "144": 135428
  }
}
```
