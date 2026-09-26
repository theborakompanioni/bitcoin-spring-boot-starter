package org.tbk.lightning.regtest.setup.devel.impl;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.regtest.setup.devel.AbstractDevelClnNodeRegistrar;

class LocalClnNodeErinRegistrar extends AbstractDevelClnNodeRegistrar {

    @Override
    protected String beanNamePrefix() {
        return "nodeErin";
    }

    @Override
    protected String hostname() {
        return "regtest_cln5_erin";
    }

    @Override
    protected ClnRpcConfig createClnRpcConfig(SslContext sslContext) {
        return ClnRpcConfigImpl.builder()
                .host("localhost")
                .port(19940)
                .sslContext(sslContext)
                .build();
    }
}
