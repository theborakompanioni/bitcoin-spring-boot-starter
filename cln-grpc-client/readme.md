cln-grpc-client
===

A module containing a CLN gRPC API client.
A Spring Boot starter is available which will automatically create injectable `NodeStub`, `NodeFutureStub` and
`NodeBlockingStub` beans.


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


## Troubleshooting

### `ManagedChannelProvider$ProviderNotFoundException`
```
Caused by: io.grpc.ManagedChannelProvider$ProviderNotFoundException: No functional channel service provider found. Try adding a dependency on the grpc-okhttp, grpc-netty, or grpc-netty-shaded artifact
```

add a channel service provider implementation, e.g.
```groovy
implementation "io.grpc:grpc-netty-shaded:${grpcVersion}"
```

Hint: The above section should currently not apply, as `grpc-netty-shaded` is included as dependency.
However, this dependency might be removed in future releases.


# Resources
- https://github.com/ElementsProject/lightning/tree/master/cln-grpc/proto
