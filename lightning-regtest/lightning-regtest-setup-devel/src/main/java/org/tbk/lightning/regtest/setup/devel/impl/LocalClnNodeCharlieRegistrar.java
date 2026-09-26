package org.tbk.lightning.regtest.setup.devel.impl;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.regtest.setup.devel.AbstractDevelClnNodeRegistrar;

class LocalClnNodeCharlieRegistrar extends AbstractDevelClnNodeRegistrar {

    @Override
    protected String beanNamePrefix() {
        return "nodeCharlie";
    }

    @Override
    protected String hostname() {
        return "regtest_cln3_charlie";
    }

    @Override
    protected ClnRpcConfig createClnRpcConfig(SslContext sslContext) {
        return ClnRpcConfigImpl.builder()
                .host("localhost")
                .port(19938)
                .sslContext(sslContext)
                .build();
    }
}
