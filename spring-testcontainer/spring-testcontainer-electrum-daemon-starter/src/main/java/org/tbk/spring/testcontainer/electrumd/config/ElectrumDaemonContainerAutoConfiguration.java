package org.tbk.spring.testcontainer.electrumd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;
import org.tbk.spring.testcontainer.eps.ElectrumPersonalServerContainer;
import org.tbk.spring.testcontainer.eps.config.ElectrumPersonalServerContainerAutoConfiguration;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.tbk.spring.testcontainer.tor.config.TorContainerAutoConfiguration;

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ElectrumDaemonContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrum-daemon.enabled", havingValue = "true")
@AutoConfigureAfter({
        BitcoindContainerAutoConfiguration.class,
        ElectrumxContainerAutoConfiguration.class,
        ElectrumPersonalServerContainerAutoConfiguration.class,
        TorContainerAutoConfiguration.class,
})
public class ElectrumDaemonContainerAutoConfiguration {

    private final ElectrumDaemonContainerProperties properties;

    private final SimpleElectrumDaemonContainerFactory containerFactory;

    public ElectrumDaemonContainerAutoConfiguration(ElectrumDaemonContainerProperties properties) {
        this.properties = requireNonNull(properties);
        this.containerFactory = new SimpleElectrumDaemonContainerFactory();
    }

    @Bean
    @ConditionalOnMissingBean(ElectrumDaemonContainerConfig.class)
    ElectrumDaemonContainerConfig electrumDaemonContainerConfig() {
        return ElectrumDaemonContainerConfig.builder()
                .environment(properties.getEnvironmentWithDefaults())
                .defaultWallet(properties.getDefaultWallet()
                        .map(it -> ElectrumDaemonContainerConfig.WalletParams.builder()
                                .walletPath(it.getWalletPath())
                                .password(it.getPassword().orElse(null))
                                .build())
                        .orElse(null))
                .wallets(properties.getWallets())
                .build();
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    @ConditionalOnBean(ElectrumxContainer.class)
    ElectrumDaemonContainer<?> electrumDaemonContainerWithElectrumxTestcontainer(ElectrumDaemonContainerConfig config,
                                                                                 ElectrumxContainer<?> electrumServer) {
        verifyCompatibilityWithElectrumx(config, electrumServer);

        ElectrumDaemonContainerConfig configWithServer = config.toBuilder()
                .server(String.format("%s:s", buildInternalContainerUrlWithoutProtocol(electrumServer, 50002)))
                .build();
        return containerFactory.createStartedElectrumDaemonContainer(configWithServer);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    @ConditionalOnBean(ElectrumPersonalServerContainer.class)
    ElectrumDaemonContainer<?> electrumDaemonContainerWithElectrumPersonalServerTestcontainer(ElectrumDaemonContainerConfig config,
                                                                                              ElectrumPersonalServerContainer<?> electrumServer) {
        ElectrumDaemonContainerConfig configWithServer = config.toBuilder()
                .server(String.format("%s:s", buildInternalContainerUrlWithoutProtocol(electrumServer, 50002)))
                .build();
        return containerFactory.createStartedElectrumDaemonContainer(configWithServer);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    @ConditionalOnBean(TorContainer.class)
    ElectrumDaemonContainer<?> electrumDaemonContainerWithTor(ElectrumDaemonContainerConfig config,
                                                              TorContainer<?> torContainer) {
        String proxy = "socks5:%s".formatted(buildInternalContainerUrlWithoutProtocol(torContainer, 9050));
        ElectrumDaemonContainerConfig configWithProxy = config.toBuilder()
                .proxy(ElectrumDaemonContainerConfig.ProxyParams.builder()
                        .proxy(proxy)
                        .build())
                .build();
        return containerFactory.createStartedElectrumDaemonContainer(configWithProxy);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    ElectrumDaemonContainer<?> electrumDaemonContainer(ElectrumDaemonContainerConfig electrumDaemonContainerConfig) {
        return containerFactory.createStartedElectrumDaemonContainer(electrumDaemonContainerConfig);
    }

    @Bean
    @ConditionalOnBean(ElectrumDaemonContainer.class)
    InitializingBean electrumDaemonContainerInitLogger(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        return () -> {
            if (log.isDebugEnabled()) {
                log.debug("Started {} with exposed ports {}", electrumDaemonContainer.getContainerName(), electrumDaemonContainer.getExposedPorts());
            }
        };
    }

    private void verifyCompatibilityWithElectrumx(ElectrumDaemonContainerConfig config,
                                                  ElectrumxContainer<?> electrumxContainer) {
        String electrumxContainerNetwork = electrumxContainer.getEnvMap().getOrDefault("NET", "regtest");
        String electrumDaemonNetwork = config.getNetwork();

        boolean networksOfClientAndServerAreCompatible = electrumDaemonNetwork.equals(electrumxContainerNetwork);
        if (!networksOfClientAndServerAreCompatible) {
            String errorMessage = String.format("Electrum Daemon and ElectrumX run on different networks! daemon: %s, server: %s", electrumDaemonNetwork, electrumxContainerNetwork);
            throw new IllegalStateException(errorMessage);
        }
    }
}
