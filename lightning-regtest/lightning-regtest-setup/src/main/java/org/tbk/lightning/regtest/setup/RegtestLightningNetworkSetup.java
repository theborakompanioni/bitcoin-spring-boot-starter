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
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.regtest.core.LightningNetworkConstants;
import org.tbk.lightning.regtest.setup.util.PaymentRouteVerifier;
import org.tbk.lightning.regtest.setup.util.RouteVerification;
import org.tbk.lightning.regtest.setup.util.SimplePaymentRouteVerifier;
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
public class RegtestLightningNetworkSetup {

    private static final PaymentRouteVerifier routeVerifier = new SimplePaymentRouteVerifier();


    private static String hex(ByteString val) {
        return HexFormat.of().formatHex(val.toByteArray());
    }

    @NonNull
    private final BitcoinExtendedClient bitcoinClient;

    @NonNull
    private final List<ChannelDefinition> channelDefinitions;

    @NonNull
    private final List<RouteVerification> routeVerifications;

    private final OnchainFaucet onchainFaucet;

    public RegtestLightningNetworkSetup(@NonNull BitcoinExtendedClient bitcoinClient, @NonNull List<ChannelDefinition> channelDefinitions, @NonNull List<RouteVerification> routeVerifications) {
        this.bitcoinClient = bitcoinClient;
        this.channelDefinitions = channelDefinitions;
        this.routeVerifications = routeVerifications;
        this.onchainFaucet = new OnchainFaucet(bitcoinClient);
    }

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

        // it seems to be necessary to mine a single block and wait for the nodes to synchronize.
        // otherwise, LND seems to be stuck on regtest indefinitely (last check: 2023-12-02).
        log.debug("Mine a single block and await node synchronization…");
        bitcoinClient.generateToAddress(1, bitcoinClient.getNewAddress());
        waitForNodesBlockHeightSynchronization();
        log.debug("Mine a single block and await node synchronization: Done.");

        log.debug("Initialize on-chain faucet and await node synchronization…");
        onchainFaucet.init();
        waitForNodesBlockHeightSynchronization();
        log.debug("Initialize on-chain faucet and await node synchronization: Done.");
    }

    private void afterSetup() {
        nodeInfos.cleanUp();
    }

    private void waitForRoutes() {
        for (RouteVerification routeVerification : routeVerifications) {
            routeVerifier.waitForRouteOrThrow(routeVerification);
        }
    }

    private List<NodeInfo> nodes() {
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

        List<Tuple2<NodeInfo, NodeInfo>> peers = ImmutableList.<Tuple2<NodeInfo, NodeInfo>>builder()
                .addAll(channelDefinitions.stream()
                        .map(it -> Tuples.of(it.getOrigin(), it.getDestination()))
                        .toList())
                .build();

        for (Tuple2<NodeInfo, NodeInfo> entry : peers) {
            String originNodeName = nodeInfos.nodeAlias(entry.getT1());
            String targetNodeName = nodeInfos.nodeAlias(entry.getT2());

            log.debug("Will now connect {} with {}…", originNodeName, targetNodeName);
            try {
                CommonConnectResponse ignoredOnPurpose = connectPeers(entry.getT1(), entry.getT2());
                log.debug("{} is connected to peer {}", originNodeName, targetNodeName);
            } catch (Exception e) {
                // LND error if peer is already connected: `UNKNOWN: already connected to peer: ${pubkey}@${ip}:${port}`
                boolean isAlreadyConnected = e.getMessage().contains("already connected to peer");
                if (!isAlreadyConnected) {
                    throw e;
                }
            }
        }

        log.debug("Successfully finished connecting peers.");
    }

    private CommonConnectResponse connectPeers(NodeInfo origin, NodeInfo dest) {
        return origin.getClient().connect(CommonConnectRequest.newBuilder()
                        .setIdentityPubkey(nodeInfos.nodeIdBytes(dest))
                        .setHost(dest.getHostname())
                        .setPort(dest.getP2pPort())
                        .build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();
    }

    private void printSetupSummary() {
        Collection<NodeInfo> nodes = nodes();

        log.info("### Network summary ###");
        for (NodeInfo node : nodes) {
            log.info("{}: {}", nodeInfos.nodeAlias(node), nodeInfos.nodeIdHex(node));
        }

        for (NodeInfo node : nodes) {
            printNodeSummary(node);
        }
        log.info("### end - Network summary - end ###");
    }

    private void printNodeSummary(NodeInfo node) {
        String nodeName = nodeInfos.nodeAlias(node);

        log.info("#### {} summary ####", nodeName);
        CommonListPeersResponse listpeersResponse = node.getClient().listPeers(CommonListPeersRequest.newBuilder().build())
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

    private void setupChannels() throws IOException {
        beforeChannelSetup();
        setupChannels(channelDefinitions);
        afterChannelSetup();
    }

    private void beforeChannelSetup() throws IOException {
        fundOnchainWallets(this.channelDefinitions);
    }

    private void afterChannelSetup() throws IOException {
        // mine a few more blocks to confirm the channels.
        // even if we do not need to mine any block, mine at least one just to confirm the nodes are syncing correctly!
        int minBlocks = 1;

        List<NodeInfo> nodesWithChannelsAwaitingConfirmation = channelDefinitions.stream()
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

    private void fundOnchainWallets(List<ChannelDefinition> definitions) throws IOException {
        List<ChannelDefinition> missingChannels = definitions.stream()
                .filter(this::needsCreation)
                .toList();

        Map<NodeInfo, Satoshi> nodesThatNeedFunding = missingChannels.stream()
                .collect(groupingBy(ChannelDefinition::getOrigin, mapping(
                        ChannelDefinition::getCapacity,
                        reducing(new Satoshi(0), Satoshi::plus)
                )));

        for (Map.Entry<NodeInfo, Satoshi> entry : nodesThatNeedFunding.entrySet()) {
            // control at least as many utxos as the amount of channels to be opened
            int minUtxos = Math.toIntExact(missingChannels.stream()
                    .filter(it -> entry.getKey().equals(it.getOrigin()))
                    .count());
            Satoshi feeBuffer = new Satoshi(21_000);
            Satoshi fundingAmount = entry.getValue().plus(feeBuffer);
            fundOnchainWallet(entry.getKey(), fundingAmount, minUtxos);
        }

        if (!nodesThatNeedFunding.isEmpty()) {
            int numBlocks = 6;
            log.debug("Will now mine {} more blocks to confirm the funding UTXOs…", numBlocks);
            Address bitcoinNodeAddress = bitcoinClient.getNewAddress();
            bitcoinClient.generateToAddress(numBlocks, bitcoinNodeAddress);
            waitForNodesBlockHeightSynchronization();
        }
    }

    private void fundOnchainWallet(NodeInfo target, Satoshi minAmount, int minUtxos) throws IOException {
        // reusing the address here is okay (not taking privacy on regtest too seriously on purpose)
        String address = target.getClient().newAddress(CommonNewAddressRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow()
                .getAddress();

        for (int i = 0; i < minUtxos; i++) {
            this.onchainFaucet.send(Address.fromString(RegTestParams.get(), address), minAmount);
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

    private void createChannel(ChannelDefinition definition) {
        Satoshi onchainFunds = fetchOnchainFunds(definition.getOrigin().getClient());
        log.debug("{} controls on-chain funds amounting to {}", nodeInfos.nodeAlias(definition.getOrigin()), onchainFunds);

        if (onchainFunds.getSat() <= definition.getCapacity().getSat()) {
            throw new IllegalStateException("Not enough funds: Cannot create channel of size " + definition.getCapacity());
        }

        CommonOpenChannelResponse openChannelResponse = definition.getOrigin().getClient().openChannel(CommonOpenChannelRequest.newBuilder()
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

    private Satoshi fetchOnchainFunds(LightningCommonClient node) {
        CommonListUnspentResponse response = node.listUnspent(CommonListUnspentRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();

        return new MilliSatoshi(response.getUnspentOutputsList().stream()
                .mapToLong(UnspentOutput::getAmountMsat)
                .sum()).truncateToSatoshi();
    }

    private List<PeerChannel> listOutgoingChannels(NodeInfo node) {
        return node.getClient().listPeerChannels(CommonListPeerChannelsRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow()
                .getPeerChannelsList().stream()
                .filter(PeerChannel::getInitiator)
                .toList();
    }

    private CommonListPeerChannelsResponse listPeerChannels(NodeInfo node) {
        return node.getClient().listPeerChannels(CommonListPeerChannelsRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();
    }

    private void waitForNodesBlockHeightSynchronization() throws IOException {
        waitForNodeBlockHeightSynchronization(bitcoinClient, nodes());
    }

    // wait for cln nodes to catch up to the newest block height.
    // prevents `io.grpc.StatusRuntimeException: RpcError { code: Some(304), message: "Still syncing with bitcoin network" }`
    private static void waitForNodeBlockHeightSynchronization(BitcoinExtendedClient bitcoin, Collection<NodeInfo> lnNodes) throws IOException {
        BlockChainInfo blockChainInfo = bitcoin.getBlockChainInfo();
        int currentBlockHeight = blockChainInfo.getBlocks();

        // does not need to be more often than every 5 seconds - cln can take quite long to synchronize.
        Duration checkInterval = Duration.ofSeconds(5);
        // cln sometimes takes up to ~30 seconds when catching up to more than 100 blocks.
        // lnd can take even longer as it fetches blocks every 1 seconds if it missed blocks via zmq.
        Duration timeout = Duration.ofMinutes(3).plusSeconds(2L * currentBlockHeight);

        lnNodes.forEach(it -> waitForNodeBlockHeightSynchronization(it.getClient(), currentBlockHeight, checkInterval, timeout));
    }

    private static void waitForNodeBlockHeightSynchronization(LightningCommonClient client,
                                                              int minBlockHeight,
                                                              Duration checkInterval,
                                                              Duration timeout) {
        Flux.interval(Duration.ZERO, checkInterval)
                .subscribeOn(Schedulers.newSingle("wait-for-block-sync"))
                .map(it -> {
                    try {
                        CommonInfoResponse info = requireNonNull(client.info(CommonInfoRequest.newBuilder().build())
                                .block(Duration.ofSeconds(30)));
                        boolean hasSyncWarning = !info.getWarningBlockSync().isEmpty();
                        boolean blockHeightInSync = info.getBlockheight() >= minBlockHeight;
                        boolean finished = !hasSyncWarning && blockHeightInSync;

                        log.debug("Waiting for block height to reach {} on {}, currently at height {} (warning={}): {}",
                                minBlockHeight, info.getAlias(), info.getBlockheight(),
                                info.getWarningBlockSync(),
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

    @RequiredArgsConstructor
    private static class OnchainFaucet {

        @NonNull
        private final BitcoinExtendedClient bitcoinClient;

        private Address address;

        public void init() throws IOException {
            BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinClient);

            this.address = bitcoinClient.getNewAddress();

            BlockChainInfo blockChainInfo = bitcoinClient.getBlockChainInfo();
            if (blockChainInfo.getBlocks() < 100) {
                bitcoinClient.generateToAddress(100 - blockChainInfo.getBlocks(), this.address);
            }
            this.mineTillBalanceIsPresent(new Satoshi(1));
        }

        private void mineTillBalanceIsPresent(Satoshi amount) throws IOException {
            Coin balance = bitcoinClient.getBalance();
            while (balance.isLessThan(Coin.ofSat(amount.getSat()))) {
                bitcoinClient.generateToAddress(1, this.address);
                balance = bitcoinClient.getBalance();
            }
        }

        public void send(Address address, Satoshi amount) throws IOException {
            requireNonNull(this.address, "`address` must not be null. Forgot to call `init`?");
            this.mineTillBalanceIsPresent(amount);
            this.bitcoinClient.sendToAddress(address, Coin.ofSat(amount.getSat()));
        }
    }

    private static class NodeInfos {

        private final LoadingCache<LightningCommonClient, CommonInfoResponse> initialClientInfo = CacheBuilder.newBuilder()
                .build(new CacheLoader<>() {
                    @Override
                    public CommonInfoResponse load(@NonNull LightningCommonClient client) {
                        return client.info(CommonInfoRequest.newBuilder().build())
                                .block(Duration.ofSeconds(30));
                    }
                });

        public CommonInfoResponse infoByNodeId(ByteString nodeId) {
            return initialClientInfo.asMap().values().stream()
                    .filter(it -> nodeId.equals(it.getIdentityPubkey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Did not find node with given id"));
        }

        public String nodeAlias(NodeInfo node) {
            return initialClientInfo.getUnchecked(node.getClient()).getAlias();
        }

        public String nodeAliasByNodeId(ByteString nodeId) {
            return infoByNodeId(nodeId).getAlias();
        }

        public ByteString nodeIdBytes(NodeInfo node) {
            return initialClientInfo.getUnchecked(node.getClient()).getIdentityPubkey();
        }

        public String nodeIdHex(NodeInfo node) {
            return hex(nodeIdBytes(node));
        }

        public void cleanUp() {
            initialClientInfo.invalidateAll();
            initialClientInfo.cleanUp();
        }
    }
}
