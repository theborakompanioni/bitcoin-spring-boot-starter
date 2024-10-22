cln-grpc-client
===

A module containing a Spring Boot Starter for a [CLN gRPC API client](https://github.com/theborakompanioni/cln-grpc-client).
The starter will automatically create injectable `NodeStub`, `NodeFutureStub` and `NodeBlockingStub` beans


## Install

[Download](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22) from Maven Central.

### Gradle

```groovy
implementation "io.github.theborakompanioni:cln-grpc-client-starter:${bitcoinSpringBootStarterVersion}"
```

### Maven
```xml
<dependency>
    <groupId>io.github.theborakompanioni</groupId>
    <artifactId>cln-grpc-client-starter</artifactId>
    <version>${bitcoinSpringBootStarter.version}</version>
</dependency>
```


## Usage

Add the following to your application properties and adapt it to your needs:
```yaml
org.tbk.lightning.cln.grpc:
  enabled: true
  host: localhost
  port: 19935
  ca-cert-file-path: '/home/user/.lightning/regtest/ca.pem'
  client-cert-file-path: '/home/user/.lightning/regtest/client.pem'
  client-key-file-path: '/home/user/.lightning/regtest/client-key.pem'
```

Of course, you can always create and inject all beans programmatically yourself.


# Resources
- cln-grpc-client (GitHub): https://github.com/theborakompanioni/cln-grpc-client
