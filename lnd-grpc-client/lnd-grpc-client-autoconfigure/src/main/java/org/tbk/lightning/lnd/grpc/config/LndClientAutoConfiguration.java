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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnd.grpc.LndRpcConfig;
import org.tbk.lightning.lnd.grpc.LndRpcConfigImpl;

import java.io.File;
import java.nio.file.Files;
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

    @Bean("lndMacaroonContext")
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.macaroonFilePath"
    })
    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public MacaroonContext lndMacaroonContext() {
        requireNonNull(properties.getMacaroonFilePath(), "'macaroonFilePath' must not be null");

        File macaroonFile = new File(properties.getMacaroonFilePath());
        checkArgument(macaroonFile.exists(), "'macaroonFile' must exist");
        checkArgument(macaroonFile.canRead(), "'macaroonFile' must be readable");

        byte[] bytes = Files.readAllBytes(macaroonFile.toPath());
        String hex = HexFormat.of().formatHex(bytes);
        return () -> hex;
    }

    @Bean("lndSslContext")
    @ConditionalOnMissingBean
    @ConditionalOnProperty({
            "org.tbk.lightning.lnd.grpc.certFilePath"
    })
    @SneakyThrows
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public SslContext lndSslContext() {
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
            "org.tbk.lightning.lnd.grpc.host",
            "org.tbk.lightning.lnd.grpc.port"
    })
    @ConditionalOnBean({MacaroonContext.class, SslContext.class})
    public LndRpcConfig lndRpcConfig(
            @Qualifier("lndMacaroonContext") MacaroonContext lndMacaroonContext,
            @Qualifier("lndSslContext") SslContext lndSslContext) {
        return LndRpcConfigImpl.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .macaroonContext(lndMacaroonContext)
                .sslContext(lndSslContext)
                .build();
    }

    // ----------- API beans ------------
    // see https://www.lightningj.org/#_available_apis for all available LightningJ APIs

    // ----------- Lightning API beans
    @Bean(name = "synchronousLndAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public SynchronousLndAPI synchronousLndAPI(LndRpcConfig rpcConfig) {
        return new SynchronousLndAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousLndAPI lndAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousWalletUnlockerAPI synchronousLndWalletUnlockerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWalletUnlockerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWalletUnlockerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousWalletUnlockerAPI lndWalletUnlockerAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousAutopilotAPI synchronousLndAutopilotAPI(LndRpcConfig rpcConfig) {
        return new SynchronousAutopilotAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndAutopilotAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousAutopilotAPI lndAutopilotAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousChainNotifierAPI synchronousLndChainNotifierAPI(LndRpcConfig rpcConfig) {
        return new SynchronousChainNotifierAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndChainNotifierAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousChainNotifierAPI lndChainNotifierAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousInvoicesAPI synchronousLndInvoiceAPI(LndRpcConfig rpcConfig) {
        return new SynchronousInvoicesAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndInvoiceAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousInvoicesAPI lndInvoiceAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousRouterAPI synchronousLndRouterAPI(LndRpcConfig rpcConfig) {
        return new SynchronousRouterAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndRouterAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousRouterAPI lndRouterAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousSignerAPI synchronousLndSignerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousSignerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndSignerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousSignerAPI lndSignerAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousWalletKitAPI synchronousLndWalletKitAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWalletKitAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWalletKitAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousWalletKitAPI lndWalletKitAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousWatchtowerAPI synchronousLndWatchtowerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWatchtowerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWatchtowerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousWatchtowerAPI lndWatchtowerAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousWatchtowerClientAPI synchronousLndWatchtowerClientAPI(LndRpcConfig rpcConfig) {
        return new SynchronousWatchtowerClientAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndWatchtowerClientAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousWatchtowerClientAPI lndWatchtowerClientAPI(LndRpcConfig rpcConfig) {
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
    public SynchronousVersionerAPI synchronousLndVersionerAPI(LndRpcConfig rpcConfig) {
        return new SynchronousVersionerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }

    @Bean(name = "lndVersionerAPI", destroyMethod = "close")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LndRpcConfig.class)
    public AsynchronousVersionerAPI lndVersionerAPI(LndRpcConfig rpcConfig) {
        return new AsynchronousVersionerAPI(
                rpcConfig.getHost(),
                rpcConfig.getPort(),
                rpcConfig.getSslContext(),
                rpcConfig.getMacaroonContext());
    }
}
