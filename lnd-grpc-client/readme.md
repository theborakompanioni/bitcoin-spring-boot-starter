lnd-grpc-client
===

A module containing a [LightningJ](https://www.lightningj.org/) LND gRPC API client.
A Spring Boot starter is available which will automatically create injectable `AsynchronousLndAPI` and
`SynchronousLndAPI` beans.


## Install

[Download](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22) from Maven Central.

### Gradle

```groovy
implementation "io.github.theborakompanioni:lnd-grpc-client-starter:${bitcoinSpringBootStarterVersion}"
```

### Maven
```xml
<dependency>
    <groupId>io.github.theborakompanioni</groupId>
    <artifactId>lnd-grpc-client</artifactId>
    <version>${bitcoinSpringBootStarter.version}</version>
</dependency>
```


## Usage

Add the following to your application properties and adapt it to your needs:
```yaml
org.tbk.lightning.lnd.grpc:
  enabled: true
  host: localhost
  port: 10009
  macaroon-file-path: '/home/user/.lnd/data/chain/bitcoin/regtest/admin.macaroon'
  cert-file-path: '/home/user/.lnd/tls.cert'
```

Of course, you can always create and inject all beans programmatically yourself.


# Resources
- API reference documentation: https://lightning.engineering/api-docs/api/lnd/#grpc
- LightningJ (GitHub): https://github.com/lightningj-org/lightningj
