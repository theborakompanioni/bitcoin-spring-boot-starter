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

Hint: The above section should currently not apply, as `grpc-netty-shaded` is included as dependency.
However, this dependency might be removed in future releases.


# Resources
- https://github.com/ElementsProject/lightning/tree/master/cln-grpc/proto
