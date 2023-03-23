package org.tbk.lightning.cln.grpc.config;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClnClientAutoConfigProperties.class)
@ConditionalOnClass(ClnRpcConfig.class)
@ConditionalOnProperty(value = "org.tbk.lightning.cln.grpc.enabled", havingValue = "true", matchIfMissing = true)
public class ClnClientAutoConfiguration {

    private final ClnClientAutoConfigProperties properties;

    public ClnClientAutoConfiguration(ClnClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.cln.grpc.host",
            "org.tbk.lightning.cln.grpc.port"
    })
    public ClnRpcConfig clnRpcConfig() {
        return ClnRpcConfigImpl.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .build();
    }


    @Bean(name = "clnChannelBuilder")
    @ConditionalOnMissingBean(name = "clnChannelBuilder")
    @ConditionalOnBean(ClnRpcConfig.class)
    public ManagedChannelBuilder<?> clnChannelBuilder(ClnRpcConfig rpcConfig,
                                                      ObjectProvider<ManagedChannelBuilderCustomizer> managedChannelBuilderCustomizer) {
        ManagedChannelBuilder<?> managedChannelBuilder = ManagedChannelBuilder.forAddress(
                rpcConfig.getHost(),
                rpcConfig.getPort()
        );

        managedChannelBuilderCustomizer.orderedStream().forEach(customizer -> customizer.customize(managedChannelBuilder));

        return managedChannelBuilder;
    }

    @Bean(name = "clnChannel")
    @ConditionalOnMissingBean(name = "clnChannel")
    @ConditionalOnBean(ManagedChannelBuilder.class)
    public ManagedChannel clnChannel(@Qualifier("clnChannelBuilder") ManagedChannelBuilder<?> clnChannelBuilder) {
        // From https://github.com/grpc/grpc-java/issues/3268#issuecomment-317484178:
        // > Channels are expensive to create, and the general recommendation is to use one per application,
        // > shared among the service stubs.
        return clnChannelBuilder.build();
    }

    @Bean(name = "clnChannelShutdownHook")
    @ConditionalOnBean(name = "clnChannel")
    @Order(value = Ordered.LOWEST_PRECEDENCE)
    public DisposableBean clnChannelShutdownHook(@Qualifier("clnChannel") ManagedChannel clnChannel) {
        return () -> {
            Duration timeout = properties.getShutdownTimeout();
            try {
                log.debug("Closing grpc managed channel {} ...", clnChannel);
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

    @Bean(name = "clnNodeBlockingStub")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "clnChannel")
    public NodeGrpc.NodeBlockingStub clnNodeBlockingStub(@Qualifier("clnChannel") ManagedChannel clnChannel) {
        return NodeGrpc.newBlockingStub(clnChannel);
    }

    @Bean(name = "clnNodeStub")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "clnChannel")
    public NodeGrpc.NodeStub clnNodeStub(@Qualifier("clnChannel") ManagedChannel clnChannel) {
        return NodeGrpc.newStub(clnChannel);
    }


    @Bean(name = "clnNodeFutureStub")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "clnChannel")
    public NodeGrpc.NodeFutureStub clnNodeFutureStub(@Qualifier("clnChannel") ManagedChannel clnChannel) {
        return NodeGrpc.newFutureStub(clnChannel);
    }
}
