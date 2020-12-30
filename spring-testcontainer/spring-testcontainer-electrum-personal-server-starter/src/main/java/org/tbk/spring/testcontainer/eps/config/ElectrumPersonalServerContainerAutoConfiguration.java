package org.tbk.spring.testcontainer.eps.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.eps.ElectrumPersonalServerContainer;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumPersonalServerContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.eps.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class ElectrumPersonalServerContainerAutoConfiguration {

    // currently only the image from "btcpayserver" is supported
    private static final String DOCKER_IMAGE_NAME = "btcpayserver/eps:0.2.0";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int hardcodedRpcPort = 50002;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(hardcodedRpcPort)
            .build();

    private final ElectrumPersonalServerContainerProperties properties;

    public ElectrumPersonalServerContainerAutoConfiguration(ElectrumPersonalServerContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "electrumPersonalServerContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumPersonalServerContainer.class)
    @ConditionalOnBean(BitcoindContainer.class)
    public ElectrumPersonalServerContainer<?> electrumPersonalServerContainerWithBitcoindTestcontainer(BitcoindContainer<?> bitcoindContainer) {
        String epsBitcoindConfig = buildEpsBitcoindConfig(bitcoindContainer);

        return createStartedContainer(epsBitcoindConfig);
    }

    @Bean(name = "electrumPersonalServerContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(ElectrumPersonalServerContainer.class)
    public ElectrumPersonalServerContainer<?> electrumPersonalServerContainer() {
        boolean isLocalhost = "localhost".equals(this.properties.getRpchost());
        boolean isLoopback = "127.0.0.1".equals(this.properties.getRpchost());
        boolean isWildcard = "0.0.0.0".equals(this.properties.getRpchost());

        boolean connectsToHost = isLocalhost || isLoopback || isWildcard;
        if (connectsToHost) {
            Testcontainers.exposeHostPorts(this.properties.getRpcport());
        }

        String epsBitcoindConfig = connectsToHost ?
                buildLocalEpsBitcoindConfig() :
                buildEpsBitcoindConfig();

        return createStartedContainer(epsBitcoindConfig);
    }

    private WaitStrategy initPhaseWaitStrategy() {
        return new LogMessageWaitStrategy()
                // we are waiting for a message like: "INFO:2020-12-29 15:57:37,721: Done.\n"
                .withRegEx("INFO:.+: Done\\.\n")
                .withTimes(1);
    }

    private WaitStrategy mainPhaseWaitStrategy() {
        return CustomHostPortWaitStrategy.builder()
                .addPort(hardcodedRpcPort)
                .build();
    }

    private ElectrumPersonalServerContainer<?> createStartedContainer(String epsBitcoindConfig) {

        ElectrumPersonalServerContainer<?> initPhaseContainer = createContainer(epsBitcoindConfig)
                .withStartupTimeout(Duration.ofMinutes(3))
                .waitingFor(initPhaseWaitStrategy());

        initPhaseContainer.start();

        // electrum personal server exits after everything is setup correctly - we need to restart it again afterwards.
        initPhaseContainer.stop();

        ElectrumPersonalServerContainer<?> mainPhaseContainer = initPhaseContainer
                .withStartupTimeout(Duration.ofMinutes(2))
                .waitingFor(mainPhaseWaitStrategy());

        // restart again - this time is should stay running
        mainPhaseContainer.start();

        // expose all mapped ports of the host so other containers can communication with the container
        MoreTestcontainers.exposeAllPortsToOtherContainers(mainPhaseContainer);

        return mainPhaseContainer;
    }

    private ElectrumPersonalServerContainer<?> createContainer(String epsBitcoindConfig) {
        Map<String, String> env = buildEnvMap(epsBitcoindConfig);

        ElectrumPersonalServerContainer<?> container = new ElectrumPersonalServerContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(env);

        return container;
    }

    private Consumer<CreateContainerCmd> cmdModifier() {
        return MoreTestcontainers.cmdModifiers().withName(dockerContainerName());
    }

    private String dockerContainerName() {
        return String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");
    }

    private Map<String, String> buildEnvMap(String epsBitcoindConfig) {
        Map<String, String> environmentWithDefaults = this.properties.getEnvironmentWithDefaults();

        String epsConfigKey = "EPS_CONFIG";

        String userGivenEpsConfig = environmentWithDefaults.getOrDefault(epsConfigKey, "");
        String enhancedEpsConfig = epsBitcoindConfig + "\n" + userGivenEpsConfig;

        Map<String, String> envWithoutEpsConfig = environmentWithDefaults.entrySet().stream()
                .filter(it -> !epsConfigKey.equals(it.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ImmutableMap.<String, String>builder()
                .putAll(envWithoutEpsConfig)
                .put(epsConfigKey, enhancedEpsConfig)
                .build();
    }


    private String buildEpsBitcoindConfig() {
        return buildEpsBitcoindConfig(
                this.properties.getRpchost(),
                this.properties.getRpcport()
        );
    }

    private String buildLocalEpsBitcoindConfig() {
        return buildEpsBitcoindConfig(
                MoreTestcontainers.testcontainersInternalHost(),
                this.properties.getRpcport()
        );
    }

    private String buildEpsBitcoindConfig(BitcoindContainer<?> bitcoindContainer) {
        return buildEpsBitcoindConfig(
                MoreTestcontainers.testcontainersInternalHost(),
                bitcoindContainer.getMappedPort(this.properties.getRpcport())
        );
    }

    private String buildEpsBitcoindConfig(String host, int port) {
        String template = "[bitcoin-rpc]\n" +
                "host = %s\n" +
                "port = %d\n" +
                "rpc_user = %s\n" +
                "rpc_password = %s\n";

        return String.format(template, host, port, this.properties.getRpcuser(), this.properties.getRpcpass());
    }
}
