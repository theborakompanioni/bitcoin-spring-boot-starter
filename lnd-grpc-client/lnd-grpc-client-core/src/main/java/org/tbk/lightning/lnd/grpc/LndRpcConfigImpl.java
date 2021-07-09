package org.tbk.lightning.lnd.grpc;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.lightningj.lnd.wrapper.MacaroonContext;

@Value
@Builder
public class LndRpcConfigImpl implements LndRpcConfig {

    /**
     * IP address or hostname including http:// or https:// where lnd daemon is reachable.
     * e.g. https://localhost:10001
     */
    @NonNull
    String rpchost;

    /**
     * Port where lnd daemon is listening.
     */
    @NonNull
    Integer rpcport;

    @NonNull
    MacaroonContext macaroonContext;

    @NonNull
    SslContext sslContext;
}
