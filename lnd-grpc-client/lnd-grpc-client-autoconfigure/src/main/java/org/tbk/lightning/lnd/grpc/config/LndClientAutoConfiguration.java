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
import org.lightningj.lnd.wrapper.autopilot.AsynchronousAutopilotAPI;
import org.lightningj.lnd.wrapper.autopilot.SynchronousAutopilotAPI;
import org.lightningj.lnd.wrapper.chainnotifier.AsynchronousChainNotifierAPI;
import org.lightningj.lnd.wrapper.chainnotifier.SynchronousChainNotifierAPI;
import org.lightningj.lnd.wrapper.invoices.AsynchronousInvoicesAPI;
import org.lightningj.lnd.wrapper.invoices.SynchronousInvoicesAPI;
import org.lightningj.lnd.wrapper.router.AsynchronousRouterAPI;
import org.lightningj.lnd.wrapper.router.SynchronousRouterAPI;
import org.lightningj.lnd.wrapper.signer.AsynchronousSignerAPI;
import org.lightningj.lnd.wrapper.signer.SynchronousSignerAPI;
import org.lightningj.lnd.wrapper.verrpc.AsynchronousVersionerAPI;
import org.lightningj.lnd.wrapper.verrpc.SynchronousVersionerAPI;
import org.lightningj.lnd.wrapper.walletkit.AsynchronousWalletKitAPI;
import org.lightningj.lnd.wrapper.walletkit.SynchronousWalletKitAPI;
import org.lightningj.lnd.wrapper.walletunlocker.AsynchronousWalletUnlockerAPI;
import org.lightningj.lnd.wrapper.walletunlocker.SynchronousWalletUnlockerAPI;
import org.lightningj.lnd.wrapper.watchtower.AsynchronousWatchtowerAPI;
import org.lightningj.lnd.wrapper.watchtower.SynchronousWatchtowerAPI;
import org.lightningj.lnd.wrapper.wtclient.AsynchronousWatchtowerClientAPI;
import org.lightningj.lnd.wrapper.wtclient.SynchronousWatchtowerClientAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HexFormat;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LndClientAutoConfigProperties.class)
@ConditionalOnClass(LndRpcConfig.class)
@ConditionalOnProperty(value = "org.tbk.lightning.lnd.grpc.enabled", havingValue = "true", matchIfMissing = true)
public class LndClientAutoConfiguration {

    private final LndClientAutoConfigProperties properties;

    public LndClientAutoConfiguration(LndClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    static class OnMacaroonSpecified extends AnyNestedCondition {

        OnMacaroonSpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.lnd.grpc.macaroon-file-path")
        static class OnFilePathSpecified {
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.lnd.grpc.macaroon-base64")
        static class OnRawValueSpecified {
        }
    }

    @Bean("lndRpcMacaroonContext")
    @ConditionalOnMissingBean(name = {"lndRpcMacaroonContext"})
    @Conditional(OnMacaroonSpecified.class)
    MacaroonContext lndRpcMacaroonContext() {
        String hex = HexFormat.of().formatHex(lndRpcMacaroon());
        return () -> hex;
    }

    private byte[] lndRpcMacaroon() {
        if (StringUtils.hasText(properties.getMacaroonFilePath())) {
            return lndRpcMacaroonFromFile();
        } else if (StringUtils.hasText(properties.getMacaroonBase64())) {
            return lndRpcMacaroonFromBase64();
        } else {
            throw new IllegalStateException("Could not find LND macaroon");
        }
    }

    private byte[] lndRpcMacaroonFromBase64() {
        requireNonNull(properties.getMacaroonBase64(), "'macaroonBase64' must not be null");
        return Base64.getDecoder().decode(properties.getMacaroonBase64());
    }

    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private byte[] lndRpcMacaroonFromFile() {
        requireNonNull(properties.getMacaroonFilePath(), "'macaroonFilePath' must not be null");

        File macaroonFile = new File(properties.getMacaroonFilePath());
        checkArgument(macaroonFile.exists(), "'macaroonFile' must exist");
        checkArgument(macaroonFile.canRead(), "'macaroonFile' must be readable");

        return Files.readAllBytes(macaroonFile.toPath());
    }

    static class OnCertSpecified extends AnyNestedCondition {

        OnCertSpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.lnd.grpc.cert-file-path")
        static class OnFilePathSpecified {
        }

        @ConditionalOnProperty(name = "org.tbk.lightning.lnd.grpc.cert-base64")
        static class OnRawValueSpecified {
        }
    }

    @Bean("lndRpcSslContext")
    @ConditionalOnMissingBean(name = {"lndRpcSslContext"})
    @Conditional(OnCertSpecified.class)
    @SneakyThrows
    SslContext lndRpcSslContext() {
        try (ByteArrayInputStream certStream = new ByteArrayInputStream(lndRpcCert())) {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(certStream)
                    .build();
        }
    }

    private byte[] lndRpcCert() {
        if (StringUtils.hasText(properties.getCertFilePath())) {
            return readAllBytes(properties.getCertFilePath(), "certFile");
        } else if (StringUtils.hasText(properties.getCertBase64())) {
            return readFromBase64(properties.getCertBase64(), "certBase64");
        } else {
            throw new IllegalStateException("Could not find LND certificate");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.host",
            "org.tbk.lightning.lnd.grpc.port"
    })
    @ConditionalOnBean(name = {"lndRpcMacaroonContext", "lndRpcSslContext"})
    LndRpcConfig lndRpcConfig(
            @Qualifier("lndRpcMacaroonContext") MacaroonContext lndRpcMacaroonContext,
            @Qualifier("lndRpcSslContext") SslContext lndRpcSslContext) {
        return LndRpcConfigImpl.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .macaroonContext(lndRpcMacaroonContext)
                .sslContext(lndRpcSslContext)
                .build();
    }

    // ----------- API beans ------------
    // see https://www.lightningj.org/#_available_apis for all available LightningJ APIs

    // ----------- Lightning API beans
    @Bean(name = "synchronousLndAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousLndAPI synchronousLndAPI(LndRpcConfig rpcConfig) {
        return new SynchronousLndAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousLndAPI lndAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousLndAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Wallet API beans
    @Bean(name = "synchronousLndWalletUnlockerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousWalletUnlockerAPI synchronousLndWalletUnlockerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWalletUnlockerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWalletUnlockerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousWalletUnlockerAPI lndWalletUnlockerAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousWalletUnlockerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Autopilot API beans
    @Bean(name = "synchronousLndAutopilotAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousAutopilotAPI synchronousLndAutopilotAPI(LndRpcConfig rpcConfig) {
        return new SynchronousAutopilotAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndAutopilotAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousAutopilotAPI lndAutopilotAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousAutopilotAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- ChainNotifier API beans
    @Bean(name = "synchronousLndChainNotifierAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousChainNotifierAPI synchronousLndChainNotifierAPI(LndRpcConfig rpcConfig) {
        return new SynchronousChainNotifierAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndChainNotifierAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousChainNotifierAPI lndChainNotifierAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousChainNotifierAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Invoices API beans
    @Bean(name = "synchronousLndInvoiceAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousInvoicesAPI synchronousLndInvoiceAPI(LndRpcConfig rpcConfig) {
        return new SynchronousInvoicesAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndInvoiceAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousInvoicesAPI lndInvoiceAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousInvoicesAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Router API beans
    @Bean(name = "synchronousLndRouterAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousRouterAPI synchronousLndRouterAPI(LndRpcConfig rpcConfig) {
        return new SynchronousRouterAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndRouterAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousRouterAPI lndRouterAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousRouterAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Signer API beans
    @Bean(name = "synchronousLndSignerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousSignerAPI synchronousLndSignerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousSignerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndSignerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousSignerAPI lndSignerAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousSignerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- WalletKit API beans
    @Bean(name = "synchronousLndWalletKitAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousWalletKitAPI synchronousLndWalletKitAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWalletKitAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWalletKitAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousWalletKitAPI lndWalletKitAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousWalletKitAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Watchtower API beans
    @Bean(name = "synchronousLndWatchtowerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousWatchtowerAPI synchronousLndWatchtowerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWatchtowerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWatchtowerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousWatchtowerAPI lndWatchtowerAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousWatchtowerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Watchtower Client API beans
    @Bean(name = "synchronousLndWatchtowerClientAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousWatchtowerClientAPI synchronousLndWatchtowerClientAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWatchtowerClientAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWatchtowerClientAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousWatchtowerClientAPI lndWatchtowerClientAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousWatchtowerClientAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    // ----------- Versioning API beans
    @Bean(name = "synchronousLndVersionerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    SynchronousVersionerAPI synchronousLndVersionerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousVersionerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndVersionerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    AsynchronousVersionerAPI lndVersionerAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousVersionerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
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