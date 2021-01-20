package org.tbk.spring.testcontainer.tor.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.tor.HiddenServiceHostnameResolver;
import org.tbk.spring.testcontainer.tor.HiddenServiceHostnames;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.tbk.spring.testcontainer.tor.config.TorContainerProperties.HiddenServiceDefinition;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
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

    private static final String HIDDEN_SERVICE_HOME = "/var/lib/tor/hidden_services";

    private final TorContainerProperties properties;

    public TorContainerAutoConfiguration(TorContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "torContainer", destroyMethod = "stop")
    @ConditionalOnMissingBean(TorContainer.class)
    public TorContainer<?> torContainer(@Qualifier("torContainerWaitStrategy") WaitStrategy waitStrategy) {
        // expose all the ports of the host that are mapped as Hidden Services
        this.properties.getHiddenServiceHostPorts().forEach(Testcontainers::exposeHostPorts);

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
    public HiddenServiceHostnameResolver hiddenServiceHostnameResolver(TorContainer<?> torContainer) {
        return new HiddenServiceHostnames(torContainer, HIDDEN_SERVICE_HOME);
    }

    @Bean
    public ApplicationRunner torContainerHiddenServiceHostnameLogger(HiddenServiceHostnameResolver resolver) {
        return args -> {
            Set<String> hiddenServiceNames = this.properties.getHiddenServices().keySet();

            Map<String, Optional<String>> serviceNameToOnionAddress = hiddenServiceNames.stream()
                    .collect(Collectors.toMap(it -> it, resolver::findHiddenServiceUrl));

            serviceNameToOnionAddress.forEach((key, value) -> {
                log.debug("{} => {}", key, value.orElse("unknown!"));
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

        String torExtraArgsEnvVarName = "TOR_EXTRA_ARGS";
        String torExtraArgs = environmentWithDefaults.get(torExtraArgsEnvVarName);

        String hiddenServiceDefinitions = this.properties.getHiddenServices().entrySet().stream()
                .map(it -> {
                    String serviceName = it.getKey();
                    HiddenServiceDefinition sd = it.getValue();
                    // since we cannot get "container.getHost()" and cannot use
                    // hostname "host.testcontainers.internal" (because tor needs ip addresses)
                    // we hardcode the ip till we have a better solution.
                    String hostIp = "172.17.0.1";

                    String hiddenServiceDir = String.format("%s/%s/", HIDDEN_SERVICE_HOME, serviceName);
                    String hiddenServicePort = String.format("%d %s:%d", sd.getVirtualPort(), hostIp, sd.getHostPort());

                    return String.format("HiddenServiceDir %s\nHiddenServicePort %s", hiddenServiceDir, hiddenServicePort);
                })
                .collect(Collectors.joining("\n"));

        String torExtraArgsWithServices = torExtraArgs + "\n" + hiddenServiceDefinitions;

        Map<String, String> environmentWithoutTorExtraArgs = environmentWithDefaults.entrySet().stream()
                .filter(it -> !torExtraArgsEnvVarName.equals(it.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ImmutableMap.<String, String>builder()
                .putAll(environmentWithoutTorExtraArgs)
                .put(torExtraArgsEnvVarName, torExtraArgsWithServices)
                .build();
    }

}
