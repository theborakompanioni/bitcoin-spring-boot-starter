spring-testcontainer-cln-starter
===

This project configures an injectable testcontainer running [CLN](https://github.com/ElementsProject/lightning).

Import the dependencies to your project, e.g.
```groovy
implementation "io.github.theborakompanioni:spring-testcontainer-bitcoind-starter:${bitcoinSpringBootStarterVersion}"
implementation "io.github.theborakompanioni:spring-testcontainer-cln-starter:${bitcoinSpringBootStarterVersion}"

// optionally add a client
implementation "io.github.theborakompanioni:cln-grpc-client-starter:${bitcoinSpringBootStarterVersion}"
```

... and add the respective properties to your configuration:
```yml
org.tbk.spring.testcontainer.cln:
  enabled: true
  port: 19735
  commands:
    - '--alias=tbk-cln-example'
    - '--bitcoin-rpcuser=myrpcuser'
    - '--bitcoin-rpcpassword=myrpcpassword'
    - '--grpc-port=19935'
```

If everything is set up correctly, you can make use of an injectable `ClnContainer` bean:
```java
@Autowired
private ClnContainer<?> clnContainer;
```

This project can be used in combination with:
- [`spring-testcontainer-bitcoind-starter`](../spring-testcontainer-bitcoind-starter)
- [`cln-grpc-client`](../../cln-grpc-client)

See [spring-testcontainer-cln-example-application](../spring-testcontainer-cln-example-application) as an example to
learn how you can use it in your application.

# Resources
- CLN (GitHub): https://github.com/ElementsProject/lightning
- CLN config values: https://lightning.readthedocs.io/lightningd-config.5.html
- [Lightning Network Development for Modern Applications](https://medium.com/lightwork/lightning-network-development-for-modern-applications-e4dd012dac82)
