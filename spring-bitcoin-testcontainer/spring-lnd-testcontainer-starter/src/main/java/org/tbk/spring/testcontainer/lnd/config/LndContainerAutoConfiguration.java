package org.tbk.spring.testcontainer.lnd.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerAutoConfiguration;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerProperties;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(LndContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.lnd.testcontainer.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoinContainerAutoConfiguration.class)
public class LndContainerAutoConfiguration {
    // currently only the image from "btcpayserver" is supported
    private static final String DOCKER_IMAGE_NAME = "lnzap/lnd:0.11.1-beta";
    //private static final String DOCKER_IMAGE_NAME = "btcpayserver/lnd:v0.11.0-beta";
    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final LndContainerProperties properties;

    public LndContainerAutoConfiguration(LndContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "lndContainer", initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> lndContainer(
            @Qualifier("bitcoinContainer") GenericContainer<?> bitcoinContainer,
            BitcoinContainerProperties bitcoinContainerProperties) {

        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(18443));
        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(28332));
        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(28333));

//        String bitcoindHost = bitcoinContainer.getHost();
        String bitcoindHost = "host.testcontainers.internal";


        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                .add("--bitcoind.rpchost=" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(18443))
                .add("--bitcoind.zmqpubrawblock=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28332))
                .add("--bitcoind.zmqpubrawtx=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(8080)
                .add(10009)
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - zeromq ports wont work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        return new GenericContainer<>(dockerImageName)
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy)
                ;
    }

    /*
        --bitcoin.active \
--bitcoin.testnet \
--debuglevel=info \
--bitcoin.node=neutrino \
--neutrino.connect=testnet1-btcd.zaphq.io \
--neutrino.connect=testnet2-btcd.zaphq.io \
--autopilot.active \
--rpclisten=0.0.0.0:10009
-----------
LND_CHAIN: "btc"
LND_ENVIRONMENT: "regtest"
LND_EXPLORERURL: "http://nbxplorer:32838/"
LND_EXTRA_ARGS: |
restlisten=0.0.0.0:8080
rpclisten=127.0.0.1:10008
rpclisten=0.0.0.0:10009
bitcoin.node=bitcoind
bitcoind.rpchost=bitcoind:43782
bitcoind.zmqpubrawblock=tcp://bitcoind:28332
bitcoind.zmqpubrawtx=tcp://bitcoind:28333
externalip=merchant_lnd:9735
no-macaroons=1
-------------

[void@localhost tmp]$ docker run -v lnd-data:/0.11.1-beta
--name=lnd-node -d
-p 9735:9735
-p 10009:10009
lnzap/lnd:0.11.1-beta
--bitcoin.active
--bitcoin.regtest
--debuglevel=info
--bitcoin.node=bitcoind
--autopilot.active=false
--rpclisten=0.0.0.0:10009
     */
    private List<String> buildCommandList() {
        List<String> fixedCommands = ImmutableList.<String>builder()
                .add("--noseedbackup")
                //.add("--alias=tbk-lnd-testcontainer-regtest")
                //.add("--debuglevel=debug")
                //.add("--debughtlc=true")
                //.add("--trickledelay=1000")
                ////.add("--maxpendingchannels=10")
                //.add("--color=#eeeeee")
                // ---------
                ////.add("--listen=9735")
                //.add("--externalip=127.0.0.1:9735")
                .add("--restlisten=0.0.0.0:8080")
                .add("--rpclisten=0.0.0.0:10009")
                // ---------
                //.add("--bitcoin.defaultchanconfs=1")
                .add("--bitcoin.active")
                .add("--bitcoin.regtest")
                .add("--bitcoin.node=bitcoind")
                //.add("--bitcoind.rpcuser=lndrpc")
                //.add("--bitcoind.rpcpass=afixedpasswordbecauselndsuckswithcookiefile")
                // ---
                //.add("--bitcoind.rpchost=host.testcontainers.internal:8332")
                //.add("--bitcoind.zmqpubrawblock=host.testcontainers.internal:28332")
                //.add("--bitcoind.zmqpubrawtx=host.testcontainers.internal:28333")
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(fixedCommands);

        this.properties.getRpcuser()
                .map(val -> String.format("--bitcoind.rpcuser=%s", val))
                .ifPresent(commandsBuilder::add);
        this.properties.getRpcpassword()
                .map(val -> String.format("--bitcoind.rpcpass=%s", val))
                .ifPresent(commandsBuilder::add);

        commandsBuilder.addAll(this.properties.getCommands());

        return commandsBuilder.build();
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class CustomHostPortWaitStrategy extends HostPortWaitStrategy {
        @Singular("addPort")
        List<Integer> ports;

        @Override
        protected Set<Integer> getLivenessCheckPorts() {
            return ports.stream()
                    .map(val -> waitStrategyTarget.getMappedPort(val))
                    .collect(Collectors.toSet());
        }
    }
}
