package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumDaemonContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrum-daemon.enabled", havingValue = "true")
public class ElectrumDaemonContainerAutoConfiguration {

    // currently only the image from "osminogin" is supported
    private static final String DOCKER_IMAGE_NAME = "osminogin/electrum-daemon:3.3.8";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final ElectrumDaemonContainerProperties properties;

    public ElectrumDaemonContainerAutoConfiguration(ElectrumDaemonContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "electrumDaemonContainer", destroyMethod = "stop")
    public ElectrumDaemonContainer<?> electrumDaemonContainer(ElectrumxContainer<?> electrumxContainer) {

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcPort())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .build();

        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        String network = electrumxContainer.getEnvMap().getOrDefault("NET", "regtest");

        ImmutableMap<String, String> env = ImmutableMap.<String, String>builder()
                .putAll(buildEnvMap())
                .put("ELECTRUM_NETWORK", network)
                .build();

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        electrumDaemonContainer.start();

        restartWithConnectingToLocalElectrumxServerHack(electrumxContainer, network, electrumDaemonContainer);

        return electrumDaemonContainer;
    }

    private Map<String, String> buildEnvMap() {
        return ImmutableMap.<String, String>builder()
                .put("ELECTRUM_USER", "test")
                .put("ELECTRUM_PASSWORD", "test")
                .build();
    }

    /**
     * Currently a ugly hack: osminogins docker image is currently not able
     * to specify the arguments during startup. A workaround is to start the container as it is
     * and then setting all necessary config options. When the daemon restarts it will pick up
     * the proper settings.
     * TODO: try to make the docker setup more customizable e.g. like lnzap/docker-lnd does.
     */
    private void restartWithConnectingToLocalElectrumxServerHack(ElectrumxContainer<?> electrumxContainer, String network, ElectrumDaemonContainer<?> electrumDaemonContainer) {
        try {
            String networkFlag = Optional.of(network)
                    .filter(it -> !"mainnet".equals(it))
                    .map(it -> "--" + it)
                    .orElse("");

            electrumDaemonContainer.execInContainer("electrum", networkFlag, "daemon", "stop");

            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "dont_show_testnet_warning", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "check_updates", "false");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "auto_connect", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "log_to_file", "true");
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "oneserver", "true");

            String electrumxHost = "host.testcontainers.internal";
            String serverUrl = String.format("%s:%s:s", electrumxHost, electrumxContainer.getMappedPort(50002));
            electrumDaemonContainer.execInContainer("electrum", networkFlag, "setconfig", "server", serverUrl);


            electrumDaemonContainer.execInContainer("electrum", networkFlag, "daemon", "start");

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Error while adapting electrum-daemon-container", e);
        }
    }

}
