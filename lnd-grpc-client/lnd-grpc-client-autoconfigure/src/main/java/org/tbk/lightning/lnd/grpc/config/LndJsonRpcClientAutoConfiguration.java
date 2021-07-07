package org.tbk.lightning.lnd.grpc.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.SneakyThrows;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndJsonRpcClientFactory;
import org.tbk.lightning.lnd.grpc.LndJsonRpcClientFactoryImpl;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.file.Files;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LndJsonRpcClientAutoConfigProperties.class)
@ConditionalOnClass(LndJsonRpcClientFactory.class)
@ConditionalOnProperty(value = "org.tbk.lightning.lnd.grpc.enabled", havingValue = "true", matchIfMissing = true)
public class LndJsonRpcClientAutoConfiguration {

    private final LndJsonRpcClientAutoConfigProperties properties;

    public LndJsonRpcClientAutoConfiguration(LndJsonRpcClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean("lndJsonRpcClientFactory")
    @ConditionalOnMissingBean
    public LndJsonRpcClientFactory lndJsonRpcClientFactory() {
        return new LndJsonRpcClientFactoryImpl();
    }

    @Bean("lndJsonRpcMacaroonContext")
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.macaroonFilePath"
    })
    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public MacaroonContext lndJsonRpcMacaroonContext() {
        requireNonNull(properties.getMacaroonFilePath(), "'macaroonFilePath' must not be null");

        File macaroonFile = new File(properties.getMacaroonFilePath());
        checkArgument(macaroonFile.exists(), "'macaroonFile' must exist");
        checkArgument(macaroonFile.canRead(), "'macaroonFile' must be readable");

        byte[] bytes = Files.readAllBytes(macaroonFile.toPath());
        String hex = DatatypeConverter.printHexBinary(bytes);
        return () -> hex;
    }

    @Bean("lndJsonRpcSslContext")
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.certFilePath"
    })
    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public SslContext lndJsonRpcSslContext() {
        requireNonNull(properties.getCertFilePath(), "'certFilePath' must not be null");

        File certFile = new File(properties.getCertFilePath());
        checkArgument(certFile.exists(), "'certFile' must exist");
        checkArgument(certFile.canRead(), "'certFile' must be readable");

        return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                .trustManager(certFile)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.rpchost",
            "org.tbk.lightning.lnd.grpc.rpcport"
    })
    @ConditionalOnBean({MacaroonContext.class, SslContext.class})
    public LndRpcConfig lndJsonRpcConfig(
            @Qualifier("lndJsonRpcMacaroonContext") MacaroonContext lndJsonRpcMacaroonContext,
            @Qualifier("lndJsonRpcSslContext") SslContext lndJsonRpcSslContext) {
        return LndRpcConfigImpl.builder()
                .rpchost(properties.getRpchost())
                .rpcport(properties.getRpcport())
                .macaroonContext(lndJsonRpcMacaroonContext)
                .sslContext(lndJsonRpcSslContext)
                .build();
    }

    @Bean(name = "synchronousLndJsonRpcClient", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public SynchronousLndAPI synchronousLndJsonRpcClient(LndJsonRpcClientFactory bitcoinClientFactory, LndRpcConfig rpcConfig) {
        return bitcoinClientFactory.createSync(rpcConfig);
    }

    @Bean(name = "lndJsonRpcClient", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousLndAPI lndJsonRpcClient(LndJsonRpcClientFactory bitcoinClientFactory, LndRpcConfig rpcConfig) {
        return bitcoinClientFactory.create(rpcConfig);
    }

}
