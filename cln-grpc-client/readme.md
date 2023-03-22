

```
Caused by: io.grpc.ManagedChannelProvider$ProviderNotFoundException: No functional channel service provider found. Try adding a dependency on the grpc-okhttp, grpc-netty, or grpc-netty-shaded artifact
```

add a channel service provider implementation, e.g.
```groovy
implementation "io.grpc:grpc-netty-shaded:${grpcVersion}"
```