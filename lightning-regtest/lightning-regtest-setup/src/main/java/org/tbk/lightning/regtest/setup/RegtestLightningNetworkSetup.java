package org.tbk.lightning.regtest.setup;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.Satoshi;
import fr.acinq.lightning.MilliSatoshi;
import io.grpc.StatusRuntimeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.regtest.core.LightningNetworkConstants;
import org.tbk.lightning.regtest.setup.util.ClnRouteVerifier;
import org.tbk.lightning.regtest.setup.util.RouteVerification;
import org.tbk.lightning.regtest.setup.util.SimpleClnRouteVerifier;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

@Slf4j
@RequiredArgsConstructor
public class RegtestLightningNetworkSetup {

    private static final ClnRouteVerifier routeVerifier = new SimpleClnRouteVerifier();

    private static String hex(ByteString val) {
        return HexFormat.of().formatHex(val.toByteArray());
    }

    @NonNull
    private final BitcoinExtendedClient bitcoinClient;

    @NonNull
    private final List<ChannelDefinition> channelDefinitions;

    @NonNull
    private final List<RouteVerification> routeVerifications;

    private final NodeInfos nodeInfos = new NodeInfos();

    public void run() throws IOException {
        log.info("Will now setup a local lightning network…");
        this.beforeSetup();
        this.setupPeers();
        this.setupChannels();
        this.printSetupSummary();
        this.waitForRoutes();
        this.afterSetup();
        log.info("Successfully finished setting up local lightning network.");
    }

    private void beforeSetup() throws IOException {
        BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinClient);

        // LND nodes need at least one block to start listening on the p2p port
        Address bitcoinNodeAddress = bitcoinClient.getNewAddress();
        bitcoinClient.generateToAddress(1, bitcoinNodeAddress);

        waitForNodesBlockHeightSynchronization();
    }

    private void afterSetup()  {
        nodeInfos.cleanUp();
    }

    private void waitForRoutes() {
        for (RouteVerification routeVerification : routeVerifications) {
            routeVerifier.waitForRouteOrThrow(routeVerification);
        }
    }

    private List<LightningCommonClient<?>> nodes() {
        return channelDefinitions.stream()
                .flatMap(it -> Stream.of(it.getOrigin(), it.getDestination()))
                .distinct()
                .sorted(Comparator.comparing(nodeInfos::nodeAlias))
                .collect(Collectors.toList());
    }

    /**
     * This method will connect all nodes provided according to the channel definitions.
     */
    private void setupPeers() {
        log.debug("Will now connect peers…");
        List<Tuple2<? extends LightningCommonClient<?>, ? extends LightningCommonClient<?>>> peers = ImmutableList.<Tuple2<? extends LightningCommonClient<?>, ? extends LightningCommonClient<?>>>builder()
                .addAll(channelDefinitions.stream()
                        .map(it -> Tuples.of(it.getOrigin(), it.getDestination()))
                        .toList())
                .addAll(routeVerifications.stream()
                        .filter(RouteVerification::isEnableConnectingPeers)
                        .map(it -> Tuples.of((LightningCommonClient<?>) it.getOrigin(), (LightningCommonClient<?>) it.getDestination()))
                        .toList())
                .build();

        for (Tuple2<? extends LightningCommonClient<?>, ? extends LightningCommonClient<?>> entry : peers) {
            String originNodeName = nodeInfos.nodeAlias(entry.getT1());
            String targetNodeName = nodeInfos.nodeAlias(entry.getT2());

            log.debug("Will now connect {} with {}…", originNodeName, targetNodeName);
            CommonConnectResponse connectResponse = connectPeers(entry.getT1(), entry.getT2());
            log.debug("{} is connected to peer {}", originNodeName, targetNodeName);
        }

        log.debug("Successfully finished connecting peers.");
    }

    private CommonConnectResponse connectPeers(LightningCommonClient<?> origin, LightningCommonClient<?> dest) {
        return origin.connect(CommonConnectRequest.newBuilder()
                        .setIdentityPubkey(nodeInfos.nodeIdBytes(dest))
                        .setHost(nodeInfos.nodeAlias(dest)) // this is a hack -> the alias is the container name!
                        .build())
                .block(Duration.ofSeconds(30));
    }

    private void setupChannels() throws IOException {
        beforeChannelSetup();
        setupChannels(channelDefinitions);
        afterChannelSetup();
    }

    private void printSetupSummary() {
        Collection<LightningCommonClient<?>> nodes = nodes();

        log.info("### Network summary ###");
        for (LightningCommonClient<?> node : nodes) {
            log.info("{}: {}", nodeInfos.nodeAlias(node), nodeInfos.nodeIdHex(node));
        }

        for (LightningCommonClient<?> node : nodes) {
            printNodeSummary(node);
        }
        log.info("### end - Network summary - end ###");
    }

    private void printNodeSummary(LightningCommonClient<?> node) {
        String nodeName = nodeInfos.nodeAlias(node);

        log.info("#### {} summary ####", nodeName);
        CommonListPeersResponse listpeersResponse = node.listPeers(CommonListPeersRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();
        log.info("  {} peers: {}", nodeName, listpeersResponse.getPeersList().stream()
                .map(it -> nodeInfos.nodeAliasByNodeId(it.getIdentityPubkey()))
                .collect(Collectors.joining(", ")));

        CommonListPeerChannelsResponse peerChannels = listPeerChannels(node);
        log.info("  {} channel count: {}", nodeName, peerChannels.getPeerChannelsCount());

        peerChannels.getPeerChannelsList().forEach(it -> {
            boolean incoming = !it.getInitiator();
            log.info("  - {} {} channel {} {}--{} {} (active: {}, capacity: {}, local_balance: {}, spendable: {}, receivable: {})",
                    incoming ? "Incoming" : "Outgoing",
                    it.getAnnounced() ? "public" : "unannounced",
                    nodeName,
                    incoming ? "<" : "", incoming ? "" : ">",
                    nodeInfos.nodeAliasByNodeId(it.getRemoteIdentityPubkey()),
                    it.getActive(),
                    new MilliSatoshi(it.getCapacityMsat()),
                    new MilliSatoshi(it.getLocalBalanceMsat()),
                    new MilliSatoshi(it.getEstimatedSpendableMsat()),
                    new MilliSatoshi(it.getEstimatedReceivableMsat()));
        });
    }

    private void afterChannelSetup() throws IOException {
        // mine a few more blocks to confirm the channels.
        // even if we do not need to mine any block, mine at least one just to confirm the nodes are syncing correctly!
        int minBlocks = 1;

        List<LightningCommonClient<?>> nodesWithChannelsAwaitingConfirmation = channelDefinitions.stream()
                .map(it -> List.of(it.getOrigin(), it.getDestination()))
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        int numBlocks = Math.max(minBlocks, nodesWithChannelsAwaitingConfirmation.stream()
                .mapToInt(it -> {
                    // TODO: get the "funding-confirms" config value form `listConfigs`, once it is available via gRPC
                    // e.g. via `it.listConfigs("funding-confirms")`
                    // till that, return the default value
                    return LightningNetworkConstants.CLN_DEFAULT_CHANNEL_FUNDING_TX_MIN_CONFIRMATIONS;
                })
                .max()
                .orElse(LightningNetworkConstants.CLN_DEFAULT_CHANNEL_FUNDING_TX_MIN_CONFIRMATIONS));

        Address bitcoinNodeAddress = bitcoinClient.getNewAddress();
        bitcoinClient.generateToAddress(numBlocks, bitcoinNodeAddress);
        waitForNodesBlockHeightSynchronization();
    }

    private void beforeChannelSetup() throws IOException {
        fundOnchainWallets(this.channelDefinitions);
    }

    private void fundOnchainWallets(List<ChannelDefinition> definitions) throws IOException {
        List<ChannelDefinition> missingChannels = definitions.stream()
                .filter(this::needsCreation)
                .toList();

        Map<LightningCommonClient<?>, Satoshi> nodesThatNeedFunding = missingChannels.stream()
                .collect(groupingBy(ChannelDefinition::getOrigin, mapping(
                        ChannelDefinition::getCapacity,
                        reducing(new Satoshi(0), Satoshi::plus)
                )));

        for (Map.Entry<LightningCommonClient<?>, Satoshi> entry : nodesThatNeedFunding.entrySet()) {
            LightningCommonClient<?> origin = entry.getKey();
            // control at least as many utxos as the amount of channels to be opened
            int minUtxos = Math.toIntExact(missingChannels.stream()
                    .filter(it -> origin.equals(it.getOrigin()))
                    .count());
            fundOnchainWallet(origin, entry.getValue(), minUtxos);
        }

        if (!nodesThatNeedFunding.isEmpty()) {
            int numBlocks = 100;
            log.debug("Will now mature coinbase outputs by mining {} more blocks…", numBlocks);
            Address bitcoinNodeAddress = bitcoinClient.getNewAddress();
            bitcoinClient.generateToAddress(numBlocks, bitcoinNodeAddress);
            waitForNodesBlockHeightSynchronization();
        }
    }

    private void setupChannels(List<ChannelDefinition> channelDefinitions) {
        for (ChannelDefinition definition : channelDefinitions) {
            createChannelIfNecessary(definition);
        }
    }

    private void createChannelIfNecessary(ChannelDefinition definition) {
        boolean needsChannelCreation = needsCreation(definition);
        if (needsChannelCreation) {
            createChannel(definition);
        }

        log.debug("Successfully finished setting up lightning channel {} -- {} --> {}: {}.",
                nodeInfos.nodeAlias(definition.getOrigin()), definition.getCapacity(),
                nodeInfos.nodeAlias(definition.getDestination()),
                needsChannelCreation ? "Created" : "Channel already present");
    }

    private boolean needsCreation(ChannelDefinition definition) {
        List<PeerChannel> outgoingChannels = listOutgoingChannels(definition.getOrigin());
        Optional<PeerChannel> channelOrEmpty = outgoingChannels.stream()
                .filter(it -> nodeInfos.nodeIdHex(definition.getDestination()).equals(hex(it.getRemoteIdentityPubkey())))
                .filter(it -> it.getCapacityMsat() == new MilliSatoshi(definition.getCapacity()).getMsat())
                .findFirst();

        return channelOrEmpty.isEmpty();
    }

    private void fundOnchainWallet(LightningCommonClient<?> target, int numBlocks) throws IOException {
        String address = target.newAddress(CommonNewAddressRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow()
                .getAddress();
        log.debug("Will fund {}'s address {} with {} block reward(s)…", nodeInfos.nodeAlias(target), address, numBlocks);
        bitcoinClient.generateToAddress(numBlocks, Address.fromString(RegTestParams.get(), address));
    }

    private void fundOnchainWallet(LightningCommonClient<?> target, Satoshi minAmount, int minUtxos) throws IOException {
        // TODO: For simplicity reasons, fund the wallet with ${minUtxos} blocks. _Might_ not be enough,
        //  but we should be safe as block rewards are 50 btc when the network initially starts.
        //  Refactor on demand!
        fundOnchainWallet(target, minUtxos);
    }

    private void createChannel(ChannelDefinition definition) {
        Satoshi onchainFunds = fetchOnchainFunds(definition.getOrigin());
        log.debug("{} controls on-chain funds amounting to {}", nodeInfos.nodeAlias(definition.getOrigin()), onchainFunds);

        if (onchainFunds.getSat() <= definition.getCapacity().getSat()) {
            throw new IllegalStateException("Not enough funds: Cannot create channel of size " + definition.getCapacity());
        }

        CommonOpenChannelResponse openChannelResponse = definition.getOrigin().openChannel(CommonOpenChannelRequest.newBuilder()
                        .setIdentityPubkey(nodeInfos.nodeIdBytes(definition.getDestination()))
                        .setAmountMsat(new MilliSatoshi(definition.getCapacity()).getMsat())
                        .setPushMsat(definition.getPushAmount().getMsat())
                        .setAnnounce(definition.isAnnounced())
                        .build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();

        String channelOutpoint = "%s:%d".formatted(hex(openChannelResponse.getTxid()), openChannelResponse.getOutputIndex());
        log.debug("Created channel with capacity {}: {} (pushed {})", definition.getCapacity(), channelOutpoint, definition.getPushAmount());
    }

    private Satoshi fetchOnchainFunds(LightningCommonClient<?> node) {
        CommonListUnspentResponse response = node.listUnspent(CommonListUnspentRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();
        ;

        return new MilliSatoshi(response.getUnspentOutputsList().stream()
                .mapToLong(UnspentOutput::getAmountMsat)
                .sum()).truncateToSatoshi();
    }

    private List<PeerChannel> listOutgoingChannels(LightningCommonClient<?> client) {
        return client.listPeerChannels(CommonListPeerChannelsRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow()
                .getPeerChannelsList().stream()
                .filter(PeerChannel::getInitiator)
                .toList();
    }

    private CommonListPeerChannelsResponse listPeerChannels(LightningCommonClient<?> client) {
        return client.listPeerChannels(CommonListPeerChannelsRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();
    }

    private void waitForNodesBlockHeightSynchronization() throws IOException {
        waitForNodeBlockHeightSynchronization(bitcoinClient, nodes());
    }

    // wait for cln nodes to catch up to the newest block height.
    // prevents `io.grpc.StatusRuntimeException: RpcError { code: Some(304), message: "Still syncing with bitcoin network" }`
    private static void waitForNodeBlockHeightSynchronization(BitcoinExtendedClient bitcoin, Collection<LightningCommonClient<?>> lnNodes) throws IOException {
        int currentBlockHeight = bitcoin.getBlockChainInfo().getBlocks();

        // does not need to be more often than every 5 seconds - cln can take quite long to synchronize
        Duration checkInterval = Duration.ofSeconds(5);
        // cln sometimes takes up to ~30 seconds when catching up to more than 100 blocks
        Duration timeout = Duration.ofSeconds(180);

        lnNodes.forEach(it -> waitForNodeBlockHeightSynchronization(it, currentBlockHeight, checkInterval, timeout));
    }

    private static void waitForNodeBlockHeightSynchronization(LightningCommonClient<?> client,
                                                              int minBlockHeight,
                                                              Duration checkInterval,
                                                              Duration timeout) {
        Flux.interval(Duration.ZERO, checkInterval)
                .subscribeOn(Schedulers.newSingle("wait-for-block-sync"))
                .map(it -> {
                    try {
                        CommonInfoResponse info = requireNonNull(client.info(CommonInfoRequest.newBuilder().build())
                                .block(Duration.ofSeconds(30)));
                        boolean finished = info.getBlockheight() >= minBlockHeight;

                        log.debug("Waiting for blockheight to reach {} on {}, currently at height {}: {}",
                                minBlockHeight, info.getAlias(), info.getBlockheight(),
                                finished ? "Done" : "Still waiting…");

                        return finished;
                    } catch (StatusRuntimeException e) {
                        log.warn("Exception while waiting for block height synchronization: {}", e.getMessage());
                        return false;
                    }
                })
                .filter(it -> it)
                .blockFirst(timeout);
    }

    private static class NodeInfos {

        private final LoadingCache<LightningCommonClient<?>, CommonInfoResponse> initialClientInfo = CacheBuilder.newBuilder()
                .build(new CacheLoader<>() {
                    @Override
                    public CommonInfoResponse load(@NonNull LightningCommonClient<?> client) {
                        return client.info(CommonInfoRequest.newBuilder().build()).block(Duration.ofSeconds(30));
                    }
                });

        public CommonInfoResponse infoByNodeId(ByteString nodeId) {
            return initialClientInfo.asMap().values().stream()
                    .filter(it -> nodeId.equals(it.getIdentityPubkey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Did not find node with given id"));
        }

        public String nodeAlias(LightningCommonClient<?> node) {
            return initialClientInfo.getUnchecked(node).getAlias();
        }

        public String nodeAliasByNodeId(ByteString nodeId) {
            return infoByNodeId(nodeId).getAlias();
        }

        public ByteString nodeIdBytes(LightningCommonClient<?> client) {
            return initialClientInfo.getUnchecked(client).getIdentityPubkey();
        }

        public String nodeIdHex(LightningCommonClient<?> client) {
            return hex(nodeIdBytes(client));
        }

        public void cleanUp() {
            initialClientInfo.invalidateAll();
            initialClientInfo.cleanUp();
        }
    }
}
