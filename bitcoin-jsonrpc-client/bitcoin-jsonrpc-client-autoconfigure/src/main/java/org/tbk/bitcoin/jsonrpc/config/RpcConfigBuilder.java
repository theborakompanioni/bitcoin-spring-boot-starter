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
    @NonNull URI uri;
    String username;
    String password;

    public RpcConfigBuilder netParams(@NonNull NetworkParameters netParams) {
        this.netParams = netParams;
        return this;
    }

    public RpcConfigBuilder uri(@NonNull URI uri) {
        this.uri = uri;
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
        return new RpcConfig(netParams, uri, username, password);
    }
}
