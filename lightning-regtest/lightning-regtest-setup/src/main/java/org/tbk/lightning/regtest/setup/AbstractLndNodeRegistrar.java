package org.tbk.lightning.regtest.setup;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.invoices.SynchronousInvoicesAPI;
import org.lightningj.lnd.wrapper.router.SynchronousRouterAPI;
import org.lightningj.lnd.wrapper.walletkit.SynchronousWalletKitAPI;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.tbk.lightning.client.common.lnd.LndCommonClient;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HexFormat;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class AbstractLndNodeRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String beanPrefix = requireNonNull(beanNamePrefix());
        BeanDefinitionCustomizer beanCustomizer = requireNonNull(beanDefinitionCustomizer());

        SslContext sslContext = createSslContext(tlsCert());
        MacaroonContext macaroonContext = createMacaroonContext(rpcMacaroon());
        LndRpcConfig rpcConfig = createLndRpcConfig(sslContext, macaroonContext);
        SynchronousLndAPI synchronousLndAPI = createSynchronousLndAPI(rpcConfig);
        SynchronousWalletKitAPI synchronousLndWalletKitAPI = createSynchronousLndWalletKitAPI(rpcConfig);
        SynchronousRouterAPI synchronousLndRouterAPI = createSynchronousLndRouterAPI(rpcConfig);
        SynchronousInvoicesAPI synchronousLndInvoicesAPI = createSynchronousLndInvoiceAPI(rpcConfig);
        LndCommonClient commonClient = new LndCommonClient(
                synchronousLndAPI,
                synchronousLndWalletKitAPI,
                synchronousLndRouterAPI,
                synchronousLndInvoicesAPI
        );

        AbstractBeanDefinition synchronousLndAPIDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SynchronousLndAPI.class, () -> synchronousLndAPI)
                .getBeanDefinition();

        AbstractBeanDefinition lndNodeCommonClientDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(LndCommonClient.class, () -> commonClient)
                .getBeanDefinition();

        beanCustomizer.customize(synchronousLndAPIDefinition);
        beanCustomizer.customize(lndNodeCommonClientDefinition);

        registry.registerBeanDefinition("%sLndNodeSynchronousLndAPI".formatted(beanPrefix), synchronousLndAPIDefinition);
        registry.registerBeanDefinition("%sLightningCommonClient".formatted(beanPrefix), lndNodeCommonClientDefinition);
    }

    protected BeanDefinitionCustomizer beanDefinitionCustomizer() {
        return bd -> {
            // empty on purpose
        };
    }

    abstract protected String beanNamePrefix();

    abstract protected LndRpcConfig createLndRpcConfig(SslContext sslContext, MacaroonContext macaroonContext);

    abstract protected byte[] tlsCert();

    abstract protected byte[] rpcMacaroon();

    private MacaroonContext createMacaroonContext(byte[] rpcMacaroon) {
        String hex = HexFormat.of().formatHex(rpcMacaroon);
        return () -> hex;
    }

    private SslContext createSslContext(byte[] tlsCert) {
        try (ByteArrayInputStream certStream = new ByteArrayInputStream(tlsCert)) {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(certStream)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SynchronousLndAPI createSynchronousLndAPI(LndRpcConfig rpcConfig) {
        return new SynchronousLndAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    private SynchronousWalletKitAPI createSynchronousLndWalletKitAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWalletKitAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    private SynchronousRouterAPI createSynchronousLndRouterAPI(LndRpcConfig rpcConfig) {
        return new SynchronousRouterAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    private SynchronousInvoicesAPI createSynchronousLndInvoiceAPI(LndRpcConfig rpcConfig) {
        return new SynchronousInvoicesAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }
}
