package org.tbk.spring.testcontainer.electrumx.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrl;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElectrumxContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrumx.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class ElectrumxContainerAutoConfiguration {

    // currently only the image from "lukechilds" is supported
    private static final String DOCKER_IMAGE_NAME = "lukechilds/electrumx:v1.15.0";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int hardcodedRpcPort = 8000;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(hardcodedRpcPort)
            .add(50001)
            .add(50002)
            .add(50004)
            .build();

    private final ElectrumxContainerProperties properties;

    public ElectrumxContainerAutoConfiguration(ElectrumxContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "electrumxContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(BitcoindContainer.class)
    public ElectrumxContainer<?> electrumxContainer(@Qualifier("electrumxContainerWaitStrategy") WaitStrategy waitStrategy) {

        boolean isLocalhost = "localhost".equals(this.properties.getRpchost());
        boolean isLoopback = "127.0.0.1".equals(this.properties.getRpchost());
        boolean isWildcard = "0.0.0.0".equals(this.properties.getRpchost());

        boolean connectsToHost = isLocalhost || isLoopback || isWildcard;
        if (connectsToHost) {
            Testcontainers.exposeHostPorts(this.properties.getRpcport());
        }

        String bitcoindDaemonUrl = connectsToHost ?
                buildLocalDaemonUrl() :
                buildDaemonUrl();

        return createStartedElectrumxContainer(bitcoindDaemonUrl, waitStrategy);
    }

    @Bean(name = "electrumxContainer", destroyMethod = "stop")
    @ConditionalOnBean(BitcoindContainer.class)
    public ElectrumxContainer<?> electrumxContainerWithBitcoindTestcontainer(@Qualifier("electrumxContainerWaitStrategy") WaitStrategy waitStrategy,
                                                                             BitcoindContainer<?> bitcoindContainer) {
        String bitcoindDaemonUrl = buildDaemonUrl(bitcoindContainer);

        return createStartedElectrumxContainer(bitcoindDaemonUrl, waitStrategy);
    }

    @Bean("electrumxContainerWaitStrategy")
    @ConditionalOnMissingBean(name = "electrumxContainerWaitStrategy")
    public WaitStrategy electrumxContainerWaitStrategy() {
        // only listen for rpc port as other ports might not be opened because of the initial sync!
        // from the docs (https://electrumx-spesmilo.readthedocs.io/en/latest/HOWTO.html#sync-progress):
        // > ElectrumX will not serve normal client connections until it has fully synchronized and caught up with
        // > your daemon. However LocalRPC connections are served at all times.
        return CustomHostPortWaitStrategy.builder()
                .addPort(hardcodedRpcPort)
                .build();
    }

    private ElectrumxContainer<?> createStartedElectrumxContainer(String bitcoindDaemonUrl, WaitStrategy waitStrategy) {

        ElectrumxContainer<?> electrumxContainer = createElectrumxContainer(bitcoindDaemonUrl, waitStrategy);

        electrumxContainer.start();

        // expose all mapped ports of the host so other containers can communication with electrumx
        MoreTestcontainers.exposeAllPortsToOtherContainers(electrumxContainer);

        return electrumxContainer;
    }

    private ElectrumxContainer<?> createElectrumxContainer(String bitcoindDaemonUrl, WaitStrategy waitStrategy) {
        Map<String, String> env = buildEnvMap(bitcoindDaemonUrl);

        ElectrumxContainer<?> electrumxContainer = new ElectrumxContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        return electrumxContainer;
    }

    private Consumer<CreateContainerCmd> cmdModifier() {
        return MoreTestcontainers.cmdModifiers().withName(dockerContainerName());
    }

    private String dockerContainerName() {
        return String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");
    }

    private Map<String, String> buildEnvMap(String bitcoindDaemonUrl) {
        Map<String, String> environmentWithDefaults = this.properties.getEnvironmentWithDefaults();

        if (environmentWithDefaults.containsKey("DAEMON_URL")) {
            throw new IllegalStateException("'DAEMON_URL' is not allowed");
        }

        return ImmutableMap.<String, String>builder()
                .putAll(environmentWithDefaults)
                .put("DAEMON_URL", bitcoindDaemonUrl)
                .build();
    }

    private String buildDaemonUrl() {
        return String.format("http://%s:%s@%s:%d",
                this.properties.getRpcuser(),
                this.properties.getRpcpass(),
                this.properties.getRpchost(),
                this.properties.getRpcport()
        );
    }

    private String buildLocalDaemonUrl() {
        return MoreTestcontainers.buildInternalHostUrl(
                "http",
                this.properties.getRpcuser(),
                this.properties.getRpcpass(),
                this.properties.getRpcport()
        );
    }

    private String buildDaemonUrl(BitcoindContainer<?> bitcoindContainer) {
        return buildInternalContainerUrl(
                bitcoindContainer,
                "http",
                this.properties.getRpcuser(),
                this.properties.getRpcpass(),
                this.properties.getRpcport()
        );
    }
}
