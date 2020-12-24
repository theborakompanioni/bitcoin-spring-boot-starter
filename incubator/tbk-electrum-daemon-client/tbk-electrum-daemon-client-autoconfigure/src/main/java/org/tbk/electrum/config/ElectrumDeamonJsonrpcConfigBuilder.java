package org.tbk.electrum.config;

import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

@Getter
public class ElectrumDeamonJsonrpcConfigBuilder {
    String host;

    Integer port;

    String username;

    String password;

    public ElectrumDeamonJsonrpcConfigBuilder host(@NonNull String host) {
        this.host = host;
        return this;
    }

    public ElectrumDeamonJsonrpcConfigBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ElectrumDeamonJsonrpcConfigBuilder username(String username) {
        this.username = username;
        return this;
    }

    public ElectrumDeamonJsonrpcConfigBuilder password(String password) {
        this.password = password;
        return this;
    }

    public ElectrumDeamonJsonrpcConfig build() {
        URI uri = URI.create(this.host + ":" + this.port);
        return new ElectrumDeamonJsonrpcConfig(uri, username, password);
    }
}
