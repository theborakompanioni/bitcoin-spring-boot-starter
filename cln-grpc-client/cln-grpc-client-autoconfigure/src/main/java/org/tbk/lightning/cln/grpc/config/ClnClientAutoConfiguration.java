package org.tbk.lightning.cln.grpc.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.SneakyThrows;
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

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
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

    @Bean("clnRpcSslContext")
    @ConditionalOnMissingBean(name = "clnRpcSslContext")
    @ConditionalOnProperty({
            "org.tbk.lightning.cln.grpc.caCertFilePath",
            "org.tbk.lightning.cln.grpc.clientCertFilePath",
            "org.tbk.lightning.cln.grpc.clientKeyFilePath"
    })
    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public SslContext clnRpcSslContext() {
        requireNonNull(properties.getCaCertFilePath(), "'caCertFilePath' must not be null");
        requireNonNull(properties.getClientCertFilePath(), "'clientCertFilePath' must not be null");
        requireNonNull(properties.getClientKeyFilePath(), "'clientKeyFilePath' must not be null");

        File caCertFile = new File(properties.getCaCertFilePath());
        checkArgument(caCertFile.exists(), "'caCertFile' must exist");
        checkArgument(caCertFile.canRead(), "'caCertFile' must be readable");

        File clientCertFile = new File(properties.getClientCertFilePath());
        checkArgument(clientCertFile.exists(), "'clientCertFile' must exist");
        checkArgument(clientCertFile.canRead(), "'clientCertFile' must be readable");

        File clientKeyFile = new File(properties.getClientKeyFilePath());
        checkArgument(clientKeyFile.exists(), "'clientKeyFile' must exist");
        checkArgument(clientKeyFile.canRead(), "'clientKeyFile' must be readable");

        return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                .keyManager(clientCertFile, clientKeyFile)
                .trustManager(caCertFile)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.cln.grpc.host",
            "org.tbk.lightning.cln.grpc.port"
    })
    @ConditionalOnBean(name = {"clnRpcSslContext"})
    public ClnRpcConfig clnRpcConfig(@Qualifier("clnRpcSslContext") SslContext clnRpcSslContext) {
        return ClnRpcConfigImpl.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .sslContext(clnRpcSslContext)
                .build();
    }

    @Bean(name = "clnChannelBuilder")
    @ConditionalOnMissingBean(name = "clnChannelBuilder")
    @ConditionalOnBean(ClnRpcConfig.class)
    public ManagedChannelBuilder<?> clnChannelBuilder(ClnRpcConfig rpcConfig,
                                                      ObjectProvider<ManagedChannelBuilderCustomizer> managedChannelBuilderCustomizer) {
        ManagedChannelBuilder<?> managedChannelBuilder = NettyChannelBuilder.forAddress(rpcConfig.getHost(), rpcConfig.getPort())
                .sslContext(rpcConfig.getSslContext());

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
