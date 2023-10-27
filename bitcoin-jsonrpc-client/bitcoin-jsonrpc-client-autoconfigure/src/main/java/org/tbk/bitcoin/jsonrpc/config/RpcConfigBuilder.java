package org.tbk.bitcoin.jsonrpc.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.NetworkParameters;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;

import java.net.URI;

@Getter
@RequiredArgsConstructor
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class RpcConfigBuilder {
    @NonNull NetworkParameters netParams;
    @NonNull String host;
    @NonNull Integer port;
    String username;
    String password;

    public RpcConfigBuilder netParams(@NonNull NetworkParameters netParams) {
        this.netParams = netParams;
        return this;
    }

    public RpcConfigBuilder host(@NonNull String host) {
        this.host = host;
        return this;
    }

    public RpcConfigBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RpcConfigBuilder username(String username) {
        this.username = username;
        return this;
    }

    public RpcConfigBuilder password(String password) {
        this.password = password;
        return this;
    }

    public RpcConfig build() {
        URI uri = URI.create("%s:%d".formatted(host, port));
        return new RpcConfig(netParams, uri, username, password);
    }
}
