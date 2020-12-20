package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
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
import java.util.Optional;
import java.util.stream.Collectors;

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

        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                // TODO: expose ports specified via auto configuration properties
                // .add("--bitcoind.rpchost=" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(18443))
                // .add("--bitcoind.zmqpubrawblock=tcp://" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(28332))
                // .add("--bitcoind.zmqpubrawtx=tcp://" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcPort())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .build();

        // only wait for rpc ports - zeromq ports wont work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        String network = electrumxContainer.getEnvMap().getOrDefault("NET", "regtest");
        ImmutableMap<String, String> env = ImmutableMap.<String, String>builder()
                .put("ELECTRUM_USER", "test")
                .put("ELECTRUM_PASSWORD", "test")
                .put("ELECTRUM_NETWORK", network)
                .build();

        ElectrumDaemonContainer<?> electrumDaemonContainer = new ElectrumDaemonContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        electrumDaemonContainer.start();

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

        return electrumDaemonContainer;
    }

    /**
     * Build command list.
     * e.g. see https://github.com/lightningnetwork/lnd/blob/master/sample-lnd.conf
     *
     * @return
     */
    private List<String> buildCommandList() {

        List<String> requiredCommands = ImmutableList.<String>builder()
                .add("--noseedbackup") // so no create/unlock wallet is needed ("lncli unlock")
                //.add("--listen=9735")
                //.add("--externalip=127.0.0.1:9735")
                //.add("--rpclisten=0.0.0.0:" + this.properties.getRpcport())
                .build();

        List<String> optionalCommands = ImmutableList.<String>builder()
                .add("--alias=" + this.properties.getCommandValueByKey("alias").orElse("tbk-lnd-testcontainer-regtest"))
                .add("--color=" + this.properties.getCommandValueByKey("color").orElse("#eeeeee"))
                .add("--debuglevel=" + this.properties.getCommandValueByKey("debuglevel").orElse("debug"))
                .add("--trickledelay=" + this.properties.getCommandValueByKey("trickledelay").orElse("1000"))
                .build();

        List<String> overridingDefaultsCommands = ImmutableList.<String>builder()
                .add("--maxpendingchannels=" + this.properties.getCommandValueByKey("maxpendingchannels").orElse("10"))
                //.add("--autopilot.active=false") <-- fails with "bool flag `--autopilot.active' cannot have an argument"
                //.add("protocol.wumbo-channels=1")
                .build();

        List<String> bitcoinCommands = ImmutableList.<String>builder()
                .add("--bitcoin.active")
                .add("--bitcoin.regtest")
                .add("--bitcoin.node=bitcoind")
                .add("--bitcoin.defaultchanconfs=" + this.properties.getCommandValueByKey("bitcoin.defaultchanconfs").orElse("1"))
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                // .addAll(requiredCommands)
                // .addAll(optionalCommands)
                // .addAll(overridingDefaultsCommands)
                // .addAll(bitcoinCommands)
                ;

        List<String> predefinedKeys = commandsBuilder.build().stream()
                .map(ElectrumdConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .map(ElectrumdConfigEntry::getName)
                .collect(Collectors.toList());

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(ElectrumdConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(ElectrumdConfigEntry::toCommandString)
                .collect(Collectors.toList());

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
    }

    @Value
    @Builder
    public static class ElectrumdConfigEntry {
        public static Optional<ElectrumdConfigEntry> valueOf(String command) {
            String commandPrefix = "--";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return ElectrumdConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=");

                        return ElectrumdConfigEntry.builder()
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
