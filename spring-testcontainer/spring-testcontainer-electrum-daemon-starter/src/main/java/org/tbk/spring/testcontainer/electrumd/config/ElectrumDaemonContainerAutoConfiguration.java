package org.tbk.spring.testcontainer.electrumd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumd.config.SimpleElectrumDaemonContainerFactory.ElectrumDaemonContainerConfig;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;
import org.tbk.spring.testcontainer.eps.ElectrumPersonalServerContainer;
import org.tbk.spring.testcontainer.eps.config.ElectrumPersonalServerContainerAutoConfiguration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElectrumDaemonContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrum-daemon.enabled", havingValue = "true")
@AutoConfigureAfter({
        BitcoindContainerAutoConfiguration.class,
        ElectrumxContainerAutoConfiguration.class,
        ElectrumPersonalServerContainerAutoConfiguration.class
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
    public ElectrumDaemonContainerConfig electrumDaemonContainerConfig() {
        return ElectrumDaemonContainerConfig.builder()
                .environment(properties.getEnvironmentWithDefaults())
                .defaultWallet(properties.getDefaultWallet().orElse(null))
                .build();
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    @ConditionalOnBean(ElectrumxContainer.class)
    public ElectrumDaemonContainer<?> electrumDaemonContainerWithElectrumxTestcontainer(ElectrumDaemonContainerConfig electrumDaemonContainerConfig,
                                                                                        ElectrumxContainer<?> electrumxContainer) {
        verifyCompatibilityWithElectrumx(electrumDaemonContainerConfig, electrumxContainer);

        return containerFactory.createStartedElectrumDaemonContainer(electrumDaemonContainerConfig, electrumxContainer);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    @ConditionalOnBean(ElectrumPersonalServerContainer.class)
    public ElectrumDaemonContainer<?> electrumDaemonContainerWithElectrumPersonalServerTestcontainer(ElectrumDaemonContainerConfig electrumDaemonContainerConfig,
                                                                                                     ElectrumPersonalServerContainer<?> electrumPersonlServerContainer) {
        return containerFactory.createStartedElectrumDaemonContainer(electrumDaemonContainerConfig, electrumPersonlServerContainer);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumDaemonContainer.class)
    public ElectrumDaemonContainer<?> electrumDaemonContainer(ElectrumDaemonContainerConfig electrumDaemonContainerConfig) {
        return containerFactory.createStartedElectrumDaemonContainer(electrumDaemonContainerConfig);
    }

    @Bean
    @ConditionalOnBean(ElectrumDaemonContainer.class)
    public InitializingBean electrumDaemonContainerInitLogger(ElectrumDaemonContainer electrumDaemonContainer) {
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
