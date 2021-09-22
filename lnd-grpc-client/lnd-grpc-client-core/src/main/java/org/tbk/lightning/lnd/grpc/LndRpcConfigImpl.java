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
     *
     * @param rpchost IP address or hostname including http:// or https:// where lnd daemon is reachable.
     * @return IP address or hostname including http:// or https:// where lnd daemon is reachable.
     */
    @NonNull
    String rpchost;

    /**
     * Port where lnd daemon is listening.
     *
     * @param rpcport Port where lnd daemon is listening.
     * @return Port where lnd daemon is listening.
     */
    @NonNull
    Integer rpcport;

    /**
     * The {@link MacaroonContext} to access the lnd api.
     *
     * @param macaroonContext The {@link MacaroonContext} to access the lnd api.
     * @return The {@link MacaroonContext} to access the lnd api.
     */
    @NonNull
    MacaroonContext macaroonContext;

    /**
     * The {@link SslContext} to access the lnd api.
     *
     * @param sslContext The {@link SslContext} to access the lnd api.
     * @return The {@link SslContext} to access the lnd api.
     */
    @NonNull
    SslContext sslContext;
}
