package org.tbk.lightning.regtest.setup.devel.impl;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;
import org.tbk.lightning.regtest.setup.devel.AbstractDevelLndNodeRegistrar;

class LocalLndNodeFaridRegistrar extends AbstractDevelLndNodeRegistrar {

    @Override
    protected String beanNamePrefix() {
        return "nodeFarid";
    }

    @Override
    protected String hostname() {
        return "regtest_lnd6_farid";
    }
    @Override
    protected LndRpcConfig createLndRpcConfig(SslContext sslContext, MacaroonContext macaroonContext) {
        return LndRpcConfigImpl.builder()
                .host("localhost")
                .port(19941)
                .sslContext(sslContext)
                .macaroonContext(macaroonContext)
                .build();
    }
}
