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
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
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

    static class OnCaCertSpecified extends AnyNestedCondition {

        OnCaCertSpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.ca-cert-file-path")
        static class OnFilePathSpecified {
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.ca-cert-base64")
        static class OnRawValueSpecified {
        }
    }

    static class OnClientCertSpecified extends AnyNestedCondition {

        OnClientCertSpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.client-cert-file-path")
        static class OnFilePathSpecified {
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.client-cert-base64")
        static class OnRawValueSpecified {
        }
    }


    static class OnClientKeySpecified extends AnyNestedCondition {

        OnClientKeySpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.client-key-file-path")
        static class OnFilePathSpecified {
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.cln.grpc.client-key-base64")
        static class OnRawValueSpecified {
        }
    }

    @Bean("clnRpcSslContext")
    @ConditionalOnMissingBean(name = "clnRpcSslContext")
    @Conditional({
            OnCaCertSpecified.class,
            OnClientCertSpecified.class,
            OnClientKeySpecified.class,
    })
    @SneakyThrows
    public SslContext clnRpcSslContext() {
        try (ByteArrayInputStream caCertStream = new ByteArrayInputStream(clnRpcCaCert());
             ByteArrayInputStream clientCertStream = new ByteArrayInputStream(clnRpcClientCert());
             ByteArrayInputStream clientKeyStream = new ByteArrayInputStream(clnRpcClientKey())) {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(caCertStream)
                    .keyManager(clientCertStream, clientKeyStream)
                    .build();
        }
    }

    private byte[] clnRpcCaCert() {
        if (StringUtils.hasText(properties.getCaCertFilePath())) {
            return readAllBytes(properties.getCaCertFilePath(), "caCertFile");
        } else if (StringUtils.hasText(properties.getCaCertBase64())) {
            return readFromBase64(properties.getCaCertBase64(), "caCertBase64");
        } else {
            throw new IllegalStateException("Could not find CLN CA certificate");
        }
    }

    private byte[] clnRpcClientCert() {
        if (StringUtils.hasText(properties.getClientCertFilePath())) {
            return readAllBytes(properties.getClientCertFilePath(), "clientCertFile");
        } else if (StringUtils.hasText(properties.getClientCertBase64())) {
            return readFromBase64(properties.getClientCertBase64(), "clientCertBase64");
        } else {
            throw new IllegalStateException("Could not find CLN client certificate");
        }
    }

    private byte[] clnRpcClientKey() {
        if (StringUtils.hasText(properties.getClientKeyFilePath())) {
            return readAllBytes(properties.getClientKeyFilePath(), "clientKeyFile");
        } else if (StringUtils.hasText(properties.getClientKeyBase64())) {
            return readFromBase64(properties.getClientKeyBase64(), "clientKeyBase64");
        } else {
            throw new IllegalStateException("Could not find CLN client key");
        }
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

    private static byte[] readFromBase64(String val, String displayName) {
        requireNonNull(val, String.format("'%s' must not be null", displayName));

        try {
            return Base64.getDecoder().decode(val);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("Error while decoding '%s'", displayName);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private static byte[] readAllBytes(String fileName, String displayName) {
        requireNonNull(fileName, String.format("'%s' must not be null", displayName));

        File file = new File(fileName);
        checkArgument(file.exists(), String.format("'%s' must exist", displayName));
        checkArgument(file.canRead(), String.format("'%s' must be readable", displayName));

        return Files.readAllBytes(file.toPath());
    }
}