package org.tbk.spring.testcontainer.tor.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(TorContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.tor.enabled", havingValue = "true")
public class TorContainerAutoConfiguration {

    // currently only the image from "btcpayserver" is supported
    private static final String DOCKER_IMAGE_NAME = "btcpayserver/tor:0.4.2.7";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private static final int hardcodedSocksPort = 9050;

    private static final List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
            .add(hardcodedSocksPort)
            .add(9051)
            .build();

    private final TorContainerProperties properties;

    public TorContainerAutoConfiguration(TorContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "torContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(TorContainer.class)
    public TorContainer<?> torContainer(@Qualifier("torContainerWaitStrategy") WaitStrategy waitStrategy) {
        return createStartedTorContainer(waitStrategy);
    }

    @Bean("torContainerWaitStrategy")
    @ConditionalOnMissingBean(name = "torContainerWaitStrategy")
    public WaitStrategy torContainerWaitStrategy() {
        return CustomHostPortWaitStrategy.builder()
                .addPort(hardcodedSocksPort)
                .build();
    }

    @Bean
    public InitializingBean torContainerHiddenServiceHostnameLogger(TorContainer<?> torContainer) {
        return () -> {
            String[] command = {"find", "/var/lib/tor/hidden_services", "-type", "f", "-name", "hostname"};
            Container.ExecResult execResult = torContainer.execInContainer(command);

            List<String> hostnameFiles = Arrays.asList(execResult.getStdout().split("\n"));

            Map<String, String> fileToContents = hostnameFiles.stream()
                    .collect(Collectors.toMap(val -> val, val -> readContentOfFileInContainerOrThrow(torContainer, val)));

            fileToContents.forEach((key, value) -> {
                log.debug("{} => {}", key, value);
            });
        };
    }

    private TorContainer<?> createStartedTorContainer(WaitStrategy waitStrategy) {

        TorContainer<?> torContainer = createTorContainer(waitStrategy);

        torContainer.start();

        // expose all mapped ports of the host so other containers can communication with tor
        MoreTestcontainers.exposeAllPortsToOtherContainers(torContainer);

        return torContainer;
    }

    private TorContainer<?> createTorContainer(WaitStrategy waitStrategy) {
        Map<String, String> env = buildEnvMap();

        TorContainer<?> torContainer = new TorContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(cmdModifier())
                .withExposedPorts(hardcodedStandardPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        return torContainer;
    }

    private Consumer<CreateContainerCmd> cmdModifier() {
        return MoreTestcontainers.cmdModifiers().withName(dockerContainerName());
    }

    private String dockerContainerName() {
        return String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");
    }

    private Map<String, String> buildEnvMap() {
        Map<String, String> environmentWithDefaults = this.properties.getEnvironmentWithDefaults();

        return ImmutableMap.<String, String>builder()
                .putAll(environmentWithDefaults)
                .build();
    }

    private String readContentOfFileInContainerOrThrow(ContainerState containerState, String file) {
        String[] command = {"cat", file};

        try {
            Container.ExecResult execResult = containerState.execInContainer(command);

            boolean success = execResult.getExitCode() == 0;
            boolean containsErrorMessage = !Strings.isNullOrEmpty(execResult.getStderr());

            if (!success || containsErrorMessage) {
                String errorMessage = String.format("Error while reading file %s in container %s: %s",
                        file, containerState.getContainerId(), execResult.getStderr());

                throw new RuntimeException(errorMessage);
            }

            String stdout = execResult.getStdout();

            boolean endsWithNewLine = stdout.endsWith("\n");
            if (endsWithNewLine) {
                return stdout.substring(0, stdout.lastIndexOf("\n"));
            }

            return stdout;
        } catch (IOException | InterruptedException e) {
            String errorMessage = String.format("Error while reading file %s in container %s",
                    file, containerState.getContainerId());

            throw new RuntimeException(errorMessage, e);
        }
    }
}
