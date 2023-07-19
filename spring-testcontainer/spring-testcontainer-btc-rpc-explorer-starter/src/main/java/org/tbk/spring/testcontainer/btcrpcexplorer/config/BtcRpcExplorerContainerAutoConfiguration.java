package org.tbk.spring.testcontainer.btcrpcexplorer.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.btcrpcexplorer.BtcRpcExplorerContainer;
import org.tbk.spring.testcontainer.btcrpcexplorer.config.BtcRpcExplorerContainerProperties.ElectrumxProperties;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BtcRpcExplorerContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.btcrpcexplorer.enabled", havingValue = "true")
@AutoConfigureAfter({
        BitcoindContainerAutoConfiguration.class,
        ElectrumxContainerAutoConfiguration.class
})
public class BtcRpcExplorerContainerAutoConfiguration {
    // currently only the image from "getumbrel" is supported
    private static final String DOCKER_IMAGE_NAME = "getumbrel/btc-rpc-explorer:v3.3.0";
    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int DEFAULT_HTTP_PORT = 3002;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(DEFAULT_HTTP_PORT)
            .build();

    private final BtcRpcExplorerContainerProperties properties;

    public BtcRpcExplorerContainerAutoConfiguration(BtcRpcExplorerContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean("btcRpcExplorerContainerWaitStrategy")
    @ConditionalOnMissingBean(name = "btcRpcExplorerContainerWaitStrategy")
    WaitStrategy btcRpcExplorerContainerWaitStrategy() {
        return CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();
    }

    @Bean
    ApplicationRunner btcRpcExplorerContainerBasicAuthPasswordLogger() {
        return args -> {
            String key = "BTCEXP_BASIC_AUTH_PASSWORD";
            String value = this.properties.getEnvironmentWithDefaults().get(key);
            log.debug("{} := {}", key, value);
        };
    }

    @Bean(name = "btcRpcExplorerContainer", destroyMethod = "stop")
    @ConditionalOnBean({BitcoindContainer.class, ElectrumxContainer.class})
    BtcRpcExplorerContainer<?> btcRpcExplorerContainerWithBitcoindAndElectrumxTestcontainer(@Qualifier("btcRpcExplorerContainerWaitStrategy") WaitStrategy waitStrategy,
                                                                                            BitcoindContainer<?> bitcoindContainer,
                                                                                            ElectrumxContainer<?> electrumxContainer) {
        String bitcoindHost = MoreTestcontainers.testcontainersInternalHost();
        Integer bitcoindPort = bitcoindContainer.getMappedPort(this.properties.getBitcoind().getRpcport());

        Integer electrumXContainerPort = Optional.ofNullable(this.properties.getElectrumx())
                .map(ElectrumxProperties::getTcpport)
                .map(electrumxContainer::getMappedPort)
                .orElse(electrumxContainer.getFirstMappedPort());

        String electrumServer = MoreTestcontainers.buildHostUrlWithoutProtocol("tcp", electrumXContainerPort);

        Map<String, String> environment = createEnvironment(bitcoindHost, bitcoindPort, Collections.singletonList(electrumServer));

        return createStartedRtcRpcExplorerContainer(waitStrategy, environment);
    }

    @Bean(name = "btcRpcExplorerContainer", destroyMethod = "stop")
    @ConditionalOnBean(BitcoindContainer.class)
    @ConditionalOnMissingBean(ElectrumxContainer.class)
    BtcRpcExplorerContainer<?> btcRpcExplorerContainerWithBitcoindTestcontainer(@Qualifier("btcRpcExplorerContainerWaitStrategy") WaitStrategy waitStrategy,
                                                                                BitcoindContainer<?> bitcoindContainer) {
        String bitcoindHost = MoreTestcontainers.testcontainersInternalHost();
        Integer bitcoindPort = bitcoindContainer.getMappedPort(this.properties.getBitcoind().getRpcport());

        Optional<String> electrumServerOrEmpty = Optional.ofNullable(this.properties.getElectrumx())
                .map(it -> "tcp://%s:%d".formatted(it.getRpchost(), it.getTcpport()));

        List<String> electrumServers = electrumServerOrEmpty
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);

        Map<String, String> environment = createEnvironment(bitcoindHost, bitcoindPort, electrumServers);

        return createStartedRtcRpcExplorerContainer(waitStrategy, environment);
    }

    @Bean(name = "btcRpcExplorerContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(BtcRpcExplorerContainer.class)
    BtcRpcExplorerContainer<?> btcRpcExplorerContainer(@Qualifier("btcRpcExplorerContainerWaitStrategy") WaitStrategy waitStrategy) {
        boolean isLocalhost = "localhost".equals(this.properties.getBitcoind().getRpchost());
        boolean isLoopback = "127.0.0.1".equals(this.properties.getBitcoind().getRpchost());
        boolean isWildcard = "0.0.0.0".equals(this.properties.getBitcoind().getRpchost());

        boolean connectsToHost = isLocalhost || isLoopback || isWildcard;

        int bitcoindPort = this.properties.getBitcoind().getRpcport();
        if (connectsToHost) {
            Testcontainers.exposeHostPorts(bitcoindPort);
        }

        String bitcoindHost = connectsToHost ?
                MoreTestcontainers.testcontainersInternalHost() :
                this.properties.getBitcoind().getRpchost();

        List<String> electrumServers = Optional.ofNullable(this.properties.getElectrumx())
                .map(it -> "%s://%s:%d".formatted("tcp", it.getRpchost(), it.getTcpport()))
                .stream()
                .toList();

        Map<String, String> environment = createEnvironment(bitcoindHost, bitcoindPort, electrumServers);

        return createStartedRtcRpcExplorerContainer(waitStrategy, environment);
    }

    private BtcRpcExplorerContainer<?> createStartedRtcRpcExplorerContainer(
            WaitStrategy waitStrategy, Map<String, String> environment
    ) {
        List<String> commands = Collections.emptyList();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        String dockerContainerName = "%s-%s".formatted(dockerImageName.getUnversionedPart(),
                        Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        BtcRpcExplorerContainer<?> btcRpcExplorerContainer = new BtcRpcExplorerContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .withEnv(environment)
                .waitingFor(waitStrategy);

        btcRpcExplorerContainer.start();

        checkState(btcRpcExplorerContainer.isRunning(), "'btcRpcExplorerContainer' must be running");

        return btcRpcExplorerContainer;
    }

    private Map<String, String> createEnvironment(String bitcoindHost, int bitcoindPort, List<String> electrumServers) {
        ImmutableMap.Builder<String, String> reservedEnvironmentBuilder = ImmutableMap.<String, String>builder()
                .put("BTCEXP_HOST", "0.0.0.0")
                .put("BTCEXP_PORT", Integer.toString(DEFAULT_HTTP_PORT))
                .put("BTCEXP_BITCOIND_HOST", bitcoindHost)
                .put("BTCEXP_BITCOIND_PORT", "" + bitcoindPort)
                .put("BTCEXP_BITCOIND_USER", this.properties.getBitcoind().getRpcuser())
                .put("BTCEXP_BITCOIND_PASS", this.properties.getBitcoind().getRpcpass());


        boolean electrumServersPresent = !electrumServers.isEmpty();
        if (electrumServersPresent) {
            reservedEnvironmentBuilder.put("BTCEXP_ELECTRUMX_SERVERS", String.join(",", electrumServers));
        }

        boolean addressApiProvided = this.properties.getEnvironmentWithDefaults().containsKey("BTCEXP_ADDRESS_API");
        boolean setupElectrumxAddressApi = !addressApiProvided && electrumServersPresent;

        if (setupElectrumxAddressApi) {
            reservedEnvironmentBuilder.put("BTCEXP_ADDRESS_API", "electrumx");
        }

        return ImmutableMap.<String, String>builder()
                .putAll(this.properties.getEnvironmentWithDefaults())
                .putAll(reservedEnvironmentBuilder.build())
                .build();
    }
}
