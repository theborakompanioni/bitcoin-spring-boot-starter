package org.tbk.lightning.regtest.setup;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.tbk.lightning.client.common.cln.ClnCommonClient;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import org.tbk.lightning.regtest.core.LightningNetworkConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class AbstractClnNodeRegistrar extends AbstractNodeRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String beanPrefix = requireNonNull(beanNamePrefix());
        BeanDefinitionCustomizer beanCustomizer = requireNonNull(beanDefinitionCustomizer());

        SslContext sslContext = createSslContext(caCert(), clientCert(), clientKey());
        ClnRpcConfig clnRpcConfig = createClnRpcConfig(sslContext);
        ManagedChannelBuilder<?> clnChannelBuilder = createClnChannelBuilder(clnRpcConfig);
        ManagedChannel clnChannel = createClnChannel(clnChannelBuilder);
        DisposableBean clnChannelShutdownHook = createClnChannelShutdownHook(clnChannel);
        NodeGrpc.NodeBlockingStub clnNodeBlockingStub = createClnNodeBlockingStub(clnChannel);
        ClnCommonClient commonClient = new ClnCommonClient(clnNodeBlockingStub);
        NodeInfo nodeInfo = NodeInfo.builder()
                .hostname(hostname())
                .p2pPort(p2pPort())
                .client(commonClient)
                .build();

        AbstractBeanDefinition nodeInfoDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(NodeInfo.class, () -> nodeInfo)
                .getBeanDefinition();

        AbstractBeanDefinition clnChannelShutdownHookDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(DisposableBean.class, () -> clnChannelShutdownHook)
                .getBeanDefinition();

        AbstractBeanDefinition clnNodeBlockingStubDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(NodeGrpc.NodeBlockingStub.class, () -> clnNodeBlockingStub)
                .getBeanDefinition();

        AbstractBeanDefinition commonClientDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(ClnCommonClient.class, () -> commonClient)
                .getBeanDefinition();

        beanCustomizer.customize(clnChannelShutdownHookDefinition);
        beanCustomizer.customize(clnNodeBlockingStubDefinition);
        beanCustomizer.customize(commonClientDefinition);

        registry.registerBeanDefinition("%sClnChannelShutdownHook".formatted(beanPrefix), clnChannelShutdownHookDefinition);
        registry.registerBeanDefinition("%sClnNodeBlockingStub".formatted(beanPrefix), clnNodeBlockingStubDefinition);
        registry.registerBeanDefinition("%sLightningCommonClient".formatted(beanPrefix), commonClientDefinition);
        registry.registerBeanDefinition("%sNodeInfo".formatted(beanPrefix), nodeInfoDefinition);
    }

    abstract protected ClnRpcConfig createClnRpcConfig(SslContext sslContext);

    abstract protected byte[] caCert();

    abstract protected byte[] clientCert();

    abstract protected byte[] clientKey();

    @Override
    protected Integer p2pPort() {
        return LightningNetworkConstants.CLN_DEFAULT_REGTEST_P2P_PORT;
    }

    private SslContext createSslContext(byte[] caCert, byte[] clientCert, byte[] clientKey) {
        try {
            try (ByteArrayInputStream caStream = new ByteArrayInputStream(caCert);
                 ByteArrayInputStream certStream = new ByteArrayInputStream(clientCert);
                 ByteArrayInputStream keyStream = new ByteArrayInputStream(clientKey);
            ) {
                return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                        .trustManager()
                        .keyManager(certStream, keyStream)
                        .trustManager(caStream)
                        .build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ManagedChannelBuilder<?> createClnChannelBuilder(ClnRpcConfig rpcConfig) {
        return NettyChannelBuilder.forAddress(rpcConfig.getHost(), rpcConfig.getPort())
                .sslContext(rpcConfig.getSslContext());
    }


    private ManagedChannel createClnChannel(ManagedChannelBuilder<?> clnChannelBuilder) {
        // From https://github.com/grpc/grpc-java/issues/3268#issuecomment-317484178:
        // > Channels are expensive to create, and the general recommendation is to use one per application,
        // > shared among the service stubs.
        return clnChannelBuilder.build();
    }

    private DisposableBean createClnChannelShutdownHook(ManagedChannel clnChannel) {
        return () -> {
            try {
                Duration timeout = Duration.ofMinutes(2);

                log.debug("Closing grpc managed channel {} …", clnChannel);
                try {
                    clnChannel.shutdown().awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
                    log.debug("Closed grpc managed channel {}", clnChannel);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.error("Error occurred closing managed grpc channel: " + e.getStatus(), e);
                    clnChannel.shutdownNow().awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted: " + e.getMessage(), e);
                    clnChannel.shutdownNow().awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("Grpc managed channel did not shutdown cleanly", e);
            }
        };
    }

    private NodeGrpc.NodeBlockingStub createClnNodeBlockingStub(ManagedChannel clnChannel) {
        return NodeGrpc.newBlockingStub(clnChannel);
    }
}
