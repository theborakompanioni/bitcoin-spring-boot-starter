spring-testcontainer-lnd-starter
===

This project configures an injectable testcontainer running [LND](https://github.com/lightningnetwork/lnd).

Import the dependencies to your project, e.g.
```groovy
implementation "io.github.theborakompanioni:spring-testcontainer-bitcoind-starter:${bitcoinSpringBootStarterVersion}"
implementation "io.github.theborakompanioni:spring-testcontainer-lnd-starter:${bitcoinSpringBootStarterVersion}"

// optionally add a client
implementation "io.github.theborakompanioni:lnd-grpc-client-starter:${bitcoinSpringBootStarterVersion}"
```

... and add the respective properties to your configuration:
```yml
org.tbk.spring.testcontainer.lnd:
  enabled: true
  restport: 19080
  rpcport: 19009
  commands:
    - '--alias=tbk-lnd-example'
    - '--bitcoind.rpcuser=myrpcuser'
    - '--bitcoind.rpcpass=myrpcpassword'
```

If everything is set up correctly, you can make use of an injectable `LndContainer` bean:
```java
@Autowired
private LndContainer<?> lndContainer;
```

This project can be used in combination with:
- [`spring-testcontainer-bitcoind-starter`](../spring-testcontainer-bitcoind-starter)
- [`lnd-grpc-client`](../../lnd-grpc-client)

See [spring-testcontainer-lnd-example-application](../spring-testcontainer-lnd-example-application) as an example to 
learn how you can use it in your application.

# Resources
- LND (GitHub): https://github.com/lightningnetwork/lnd
- LND config values: https://github.com/lightningnetwork/lnd/blob/master/sample-lnd.conf
- LND Overview and Developer Guide: https://dev.lightning.community/overview
- Working with lnd and Docker: https://dev.lightning.community/guides/docker/
- lnd docker readme.md
  - https://github.com/lightningnetwork/lnd/blob/master/docker/README.md
  - https://github.com/lightningnetwork/lnd/blob/master/docs/DOCKER.md
- [Lightning Network Development for Modern Applications](https://medium.com/lightwork/lightning-network-development-for-modern-applications-e4dd012dac82)
