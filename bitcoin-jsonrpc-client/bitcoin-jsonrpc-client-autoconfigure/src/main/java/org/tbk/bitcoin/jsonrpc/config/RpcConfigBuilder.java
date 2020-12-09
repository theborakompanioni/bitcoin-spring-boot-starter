package org.tbk.bitcoin.jsonrpc.config;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.NetworkParameters;

import java.net.URI;

@Getter
@RequiredArgsConstructor
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
        URI uri = URI.create(this.host + ":" + this.port);
        return new RpcConfig(netParams, uri, username, password);
    }
}
