cln-grpc-client
===


## Troubleshooting

### `ManagedChannelProvider$ProviderNotFoundException`
```
Caused by: io.grpc.ManagedChannelProvider$ProviderNotFoundException: No functional channel service provider found. Try adding a dependency on the grpc-okhttp, grpc-netty, or grpc-netty-shaded artifact
```

add a channel service provider implementation, e.g.
```groovy
implementation "io.grpc:grpc-netty-shaded:${grpcVersion}"
```


# Resources
- https://github.com/ElementsProject/lightning/tree/master/cln-grpc/proto
