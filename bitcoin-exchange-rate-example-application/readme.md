Example application for monetary conversion

`GET /api/v1/exchange`:
```json
{
  "providerNames":["ECB-HIST90","IMF","IDENT","ECB-HIST","ECB"],
  "providerChain":["IDENT","ECB","IMF","ECB-HIST","ECB-HIST90"]
}
```

`GET /api/v1/exchange/latest?target=USD&target=CHF`:
```json
{
  "base":"EUR",
  "rates":[
    {"base":"EUR","derived":false,"factor":1.1698,"provider":"ECB","target":"USD","type":"DEFERRED"},
    {"base":"EUR","derived":false,"factor":1.0697,"provider":"ECB","target":"CHF","type":"DEFERRED"}
  ]
}
```