package org.tbk.electrum.config;

import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
public class ElectrumDaemonJsonrpcConfig {
    @NonNull
    URI uri;

    @NonNull
    String username;

    @NonNull
    String password;
}
