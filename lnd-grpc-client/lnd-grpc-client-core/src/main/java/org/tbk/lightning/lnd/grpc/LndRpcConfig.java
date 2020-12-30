package org.tbk.lightning.lnd.grpc;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.lightningj.lnd.wrapper.MacaroonContext;

public interface LndRpcConfig {

    /**
     * IP address or hostname including http:// or https://
     * where lnd daemon is reachable
     * e.g. https://localhost:10001
     */
    String getRpchost();

    /**
     * Port where lnd daemon ist listening
     */
    Integer getRpcport();

    MacaroonContext getMacaroonContext();

    SslContext getSslContext();
}
