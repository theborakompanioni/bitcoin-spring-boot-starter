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
     * IP address or hostname where lnd daemon is reachable.
     * e.g. localhost, 192.168.0.1
     *
     * @param host IP address or hostname where lnd daemon is reachable.
     * @return IP address or hostname where lnd daemon is reachable.
     */
    @NonNull
    String host;

    /**
     * Port where lnd daemon is listening.
     *
     * @param port Port where lnd daemon is listening.
     * @return Port where lnd daemon is listening.
     */
    @NonNull
    Integer port;

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
