package org.tbk.lightning.regtest.setup.devel.impl;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import org.tbk.lightning.regtest.core.LightningNetworkConstants;
import org.tbk.lightning.regtest.setup.ChannelDefinition;
import org.tbk.lightning.regtest.setup.RegtestLightningNetworkSetup;
import org.tbk.lightning.regtest.setup.util.RouteVerification;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * Spring configuration that connects to running lightning nodes
 * defined in {@see ./lightning-regtest-setup-devel/docker/docker-compose.yml}.
 * Beans of type {@link NodeGrpc.NodeBlockingStub} are registered and added to the application context,
 * as well as a {@link RegtestLightningNetworkSetup} in order to create balanced lightning channels between the nodes.
 */
@Slf4j
@Import({
        LocalClnNodeAppRegistrar.class,
        LocalClnNodeAliceRegistrar.class,
        LocalClnNodeBobRegistrar.class,
        LocalClnNodeCharlieRegistrar.class,
        LocalClnNodeDaveRegistrar.class,
        LocalClnNodeErinRegistrar.class,
})
@Configuration(proxyBeanMethods = false)
public class LocalRegtestLightningNetworkSetupConfig {

    @Bean
    RpcConfig bitcoinRpcConfig() {
        String host = "localhost";
        int port = 18443;
        String username = "regtest-rpc-user";
        String password = "regtest-rpc-pass";
        URI uri = URI.create("http://%s:%d".formatted(host, port));
        return new RpcConfig(RegTestParams.get(), uri, username, password);
    }

    @Bean
    BitcoinExtendedClient bitcoinRegtestClient(RpcConfig rpcConfig) {
        return new BitcoinExtendedClient(rpcConfig);
    }

    /**
     * Create a local lightning network according to `./README.md`
     */
    @Bean
    RegtestLightningNetworkSetup regtestLightningNetworkSetup(BitcoinExtendedClient bitcoinRegtestClient,
                                                              @Qualifier("nodeAppClnNodeBlockingStub") NodeGrpc.NodeBlockingStub appClnNode,
                                                              @Qualifier("nodeAliceClnNodeBlockingStub") NodeGrpc.NodeBlockingStub aliceClnNode,
                                                              @Qualifier("nodeBobClnNodeBlockingStub") NodeGrpc.NodeBlockingStub bobClnNode,
                                                              @Qualifier("nodeCharlieClnNodeBlockingStub") NodeGrpc.NodeBlockingStub charlieClnNode,
                                                              @Qualifier("nodeErinClnNodeBlockingStub") NodeGrpc.NodeBlockingStub erinClnNode) throws IOException {
        RegtestLightningNetworkSetup regtestLightningNetworkSetup = new RegtestLightningNetworkSetup(
                bitcoinRegtestClient,
                ImmutableList.<ChannelDefinition>builder()
                        // app -> alice
                        .add(ChannelDefinition.builder()
                                .origin(appClnNode)
                                .destination(aliceClnNode)
                                .capacity(LightningNetworkConstants.LARGEST_CHANNEL_SIZE)
                                .pushAmount(LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.div(4))
                                .build())
                        // app -> bob
                        .add(ChannelDefinition.builder()
                                .origin(appClnNode)
                                .destination(bobClnNode)
                                .capacity(LightningNetworkConstants.LARGEST_CHANNEL_SIZE.div(2))
                                .pushAmount(LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.div(2).div(4))
                                .build())
                        // bob -> charlie
                        .add(ChannelDefinition.builder()
                                .origin(bobClnNode)
                                .destination(charlieClnNode)
                                .capacity(LightningNetworkConstants.LARGEST_CHANNEL_SIZE.div(4))
                                .pushAmount(LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.div(4).div(4))
                                .build())
                        // charlie -> erin (unannounced channel!)
                        .add(ChannelDefinition.builder()
                                .origin(charlieClnNode)
                                .destination(erinClnNode)
                                .capacity(LightningNetworkConstants.LARGEST_CHANNEL_SIZE.div(8))
                                .pushAmount(LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.div(8).div(4))
                                .announced(false)
                                .build())
                        .build(),
                ImmutableList.<RouteVerification>builder()
                        // app -> charlie
                        .add(RouteVerification.builder()
                                .origin(appClnNode)
                                .destination(charlieClnNode)
                                .checkInterval(Duration.ofSeconds(2))
                                .timeout(Duration.ofMinutes(5))
                                .build())
                        .build()
        );

        regtestLightningNetworkSetup.run();

        return regtestLightningNetworkSetup;
    }
}
