package org.tbk.electrum.config;

import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

@Getter
public class ElectrumDaemonJsonrpcConfigBuilder {
    String host;

    Integer port;

    String username;

    String password;

    public ElectrumDaemonJsonrpcConfigBuilder host(@NonNull String host) {
        this.host = host;
        return this;
    }

    public ElectrumDaemonJsonrpcConfigBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ElectrumDaemonJsonrpcConfigBuilder username(String username) {
        this.username = username;
        return this;
    }

    public ElectrumDaemonJsonrpcConfigBuilder password(String password) {
        this.password = password;
        return this;
    }

    public ElectrumDaemonJsonrpcConfig build() {
        URI uri = URI.create("%s:%d".formatted(host, port));
        return new ElectrumDaemonJsonrpcConfig(uri, username, password);
    }
}
