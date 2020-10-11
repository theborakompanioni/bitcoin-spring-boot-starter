[![Build Status](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter.svg?branch=master)](https://www.travis-ci.org/theborakompanioni/spring-boot-bitcoin-starter)
[![License](https://img.shields.io/github/license/theborakompanioni/spring-boot-bitcoin-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE)

spring-boot-bitcoin-starter
===

 
A spring boot starter project with convenient dependency descriptors for 
[ConsensusJ](https://github.com/ConsensusJ/consensusj) modules that you can include in your application.


##### bitcoin-jsonrpc-client-starter
Starter for a Bitcoin Core JSON-RPC API client.
This will automatically create an autowireable `BitcoinClient` bean:

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

# License
The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.