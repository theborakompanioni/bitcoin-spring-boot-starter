package org.tbk.spring.testcontainer.cln.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.cln.ClnContainer;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClnContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.cln.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class ClnContainerAutoConfiguration {

    private static final String DOCKER_IMAGE_NAME = "elementsproject/lightningd:v22.11.1";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final ClnContainerProperties properties;

    public ClnContainerAutoConfiguration(ClnContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "clnContainer", initMethod = "start", destroyMethod = "stop")
    public ClnContainer<?> clnContainer(BitcoindContainer<?> bitcoindContainer) {
        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                // TODO: https://github.com/ElementsProject/lightning/issues/5845#issuecomment-1368448120
                // --bitcoin-rpcconnect=ip.address.of.bitcoind, --bitcoin-rpcuser and --bitcoin-rpcpassword
                .add("--bitcoin-rpcconnect=" + MoreTestcontainers.testcontainersInternalHost())
                .add("--bitcoin-rpcport=" + bitcoindContainer.getMappedPort(18443))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcport())
                .add(this.properties.getPort())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports
        WaitStrategy portWaitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        WaitStrategy waitStrategy = new WaitAllStrategy(WaitAllStrategy.Mode.WITH_OUTER_TIMEOUT)
                //.withStrategy(portWaitStrategy)
                .withStrategy(Wait.forLogMessage(".*Server started with public key.*", 1))
                .withStartupTimeout(ClnContainerProperties.DEFAULT_STARTUP_TIMEOUT);

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                        Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        return new ClnContainer<>(dockerImageName)
                .dependsOn(bitcoindContainer)
                .withEnv(ImmutableMap.<String, String>builder()
                        .put("LIGHTNINGD_DATA", "/root/.lightning")
                        .put("LIGHTNINGD_PORT", String.valueOf(this.properties.getPort()))
                        .put("LIGHTNINGD_RPC_PORT", String.valueOf(this.properties.getRpcport()))
                        .put("LIGHTNINGD_NETWORK", "regtest")
                        .build())
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy);
    }

    /**
     * Build command list.
     * e.g. see https://lightning.readthedocs.io/lightningd-config.5.html
     *
     * @return a list fo commands for the container.
     */
    private List<String> buildCommandList() {

        List<String> requiredCommands = ImmutableList.<String>builder()
                .add("--disable-dns")
                //.add("--noseedbackup") // so no create/unlock wallet is needed ("lncli unlock")
                //.add("--listen=9735")
                //.add("--externalip=127.0.0.1:9735")
                //.add("--restlisten=0.0.0.0:" + this.properties.getRestport())
                //.add("--rpclisten=0.0.0.0:" + this.properties.getRpcport())
                .build();

        List<String> optionalCommands = ImmutableList.<String>builder()
                //.add("--alias=" + this.properties.getCommandValueByKey("alias").orElse("tbk-lnd-testcontainer-regtest"))
                .add("--rgb=" + this.properties.getCommandValueByKey("rgb").orElse("cccccc"))
                .add("--log-level=" + this.properties.getCommandValueByKey("log-level").orElse("debug"))
                //.add("--trickledelay=" + this.properties.getCommandValueByKey("trickledelay").orElse("1000"))
                .build();

        List<String> overridingDefaultsCommands = ImmutableList.<String>builder()
                //.add("--maxpendingchannels=" + this.properties.getCommandValueByKey("maxpendingchannels").orElse("10"))
                //.add("--autopilot.active=false") <-- fails with "bool flag `--autopilot.active' cannot have an argument"
                //.add("protocol.wumbo-channels=1")
                .build();

        List<String> bitcoinCommands = ImmutableList.<String>builder()
                .add("--bitcoin-retry-timeout=" + Duration.ofSeconds(60).toSeconds())
                //.add("--bitcoin.active")
                //.add("--bitcoin.regtest")
                //.add("--bitcoin.node=bitcoind")
                //.add("--bitcoin.defaultchanconfs=" + this.properties.getCommandValueByKey("bitcoin.defaultchanconfs").orElse("1"))
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(requiredCommands)
                .addAll(optionalCommands)
                .addAll(overridingDefaultsCommands)
                .addAll(bitcoinCommands);

        List<String> predefinedKeys = commandsBuilder.build().stream()
                .map(ClnConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .map(ClnConfigEntry::getName)
                .toList();

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(ClnConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(ClnConfigEntry::toCommandString)
                .toList();

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
    }

    @Value
    @Builder
    public static class ClnConfigEntry {
        public static Optional<ClnConfigEntry> valueOf(String command) {
            String commandPrefix = "--";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix, 2)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return ClnConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=", 2);

                        return ClnConfigEntry.builder()
                                .name(parts[0])
                                .value(parts[1])
                                .build();
                    });
        }

        @NonNull
        String name;

        String value;

        public Optional<String> getValue() {
            return Optional.ofNullable(value);
        }

        public String toCommandString() {
            return "--" + this.name + getValue()
                    .map(it -> "=" + it)
                    .orElse("");
        }
    }

}
