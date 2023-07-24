package org.tbk.lightning.regtest.setup.devel.impl;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.regtest.setup.devel.AbstractDevelClnNodeRegistrar;

class LocalClnNodeBobRegistrar extends AbstractDevelClnNodeRegistrar {

    @Override
    protected String beanNamePrefix() {
        return "nodeBob";
    }

    @Override
    protected ClnRpcConfig createClnRpcConfig(SslContext sslContext) {
        return ClnRpcConfigImpl.builder()
                .host("localhost")
                .port(19937)
                .sslContext(sslContext)
                .build();
    }
}
