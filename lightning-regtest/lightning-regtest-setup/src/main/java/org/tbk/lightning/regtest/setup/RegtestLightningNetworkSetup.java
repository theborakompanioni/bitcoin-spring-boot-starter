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
import org.tbk.lightning.cln.grpc.client.*;
import org.tbk.lightning.cln.grpc.client.NodeGrpc.NodeBlockingStub;
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

    private final LoadingCache<NodeBlockingStub, GetinfoResponse> initialClientInfo = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public GetinfoResponse load(@NonNull NodeBlockingStub client) {
                    return client.getinfo(GetinfoRequest.newBuilder().build());
                }
            });

    public void run() throws IOException {
        log.info("Will now setup a local lightning network…");
        this.setupPeers();
        this.setupChannels();
        this.printSetupSummary();
        this.waitForRoutes();
        log.info("Successfully finished setting up local lightning network.");

        initialClientInfo.invalidateAll();
        initialClientInfo.cleanUp();
    }

    private void waitForRoutes() {
        for (RouteVerification routeVerification : routeVerifications) {
            routeVerifier.waitForRouteOrThrow(routeVerification);
        }
    }

    private String nodeName(NodeBlockingStub clnNode) {
        return initialClientInfo.getUnchecked(clnNode).getAlias();
    }

    private GetinfoResponse infoByNodeId(ByteString nodeId) {
        return initialClientInfo.asMap().values().stream()
                .filter(it -> nodeId.equals(it.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Did not find node with given id"));
    }

    private String nodeNameByNodeId(ByteString nodeId) {
        return infoByNodeId(nodeId).getAlias();
    }

    private ByteString nodeIdBytes(NodeBlockingStub clnNode) {
        return initialClientInfo.getUnchecked(clnNode).getId();
    }

    private String nodeIdHex(NodeBlockingStub clnNode) {
        return hex(nodeIdBytes(clnNode));
    }

    private List<NodeBlockingStub> nodes() {
        return channelDefinitions.stream()
                .flatMap(it -> Stream.of(it.getOrigin(), it.getDestination()))
                .distinct()
                .sorted(Comparator.comparing(this::nodeName))
                .collect(Collectors.toList());
    }

    /**
     * This method will connect all nodes provided according to the channel definitions.
     */
    private void setupPeers() {
        log.debug("Will now connect peers…");

        List<Tuple2<NodeBlockingStub, NodeBlockingStub>> peers = ImmutableList.<Tuple2<NodeBlockingStub, NodeBlockingStub>>builder()
                .addAll(channelDefinitions.stream()
                        .map(it -> Tuples.of(it.getOrigin(), it.getDestination()))
                        .toList())
                .addAll(routeVerifications.stream()
                        .filter(RouteVerification::isEnableConnectingPeers)
                        .map(it -> Tuples.of(it.getOrigin(), it.getDestination()))
                        .toList())
                .build();

        for (Tuple2<NodeBlockingStub, NodeBlockingStub> entry : peers) {
            log.debug("Will now connect {} with {}…", nodeName(entry.getT1()), nodeName(entry.getT2()));
            ConnectResponse connectResponse = connectPeers(entry.getT1(), entry.getT2());
            log.debug("{} is connected to peer {}: {}", nodeName(entry.getT1()), nodeName(entry.getT2()),
                    connectResponse.getDirection());
        }

        log.debug("Successfully finished connecting peers.");
    }

    private ConnectResponse connectPeers(NodeBlockingStub origin, NodeBlockingStub dest) {
        return origin.connectPeer(ConnectRequest.newBuilder()
                .setId(nodeIdHex(dest))
                .setHost(nodeName(dest)) // this is a hack -> the alias is the container name!
                .build());
    }

    private void setupChannels() throws IOException {
        beforeChannelSetup();
        setupChannels(channelDefinitions);
        afterChannelSetup();
    }

    private void printSetupSummary() {
        Collection<NodeBlockingStub> nodes = nodes();

        log.info("### Network summary ###");
        for (NodeBlockingStub node : nodes) {
            log.info("{}: {}", nodeName(node), nodeIdHex(node));
        }

        for (NodeBlockingStub node : nodes) {
            printNodeSummary(node);
        }
        log.info("### end - Network summary - end ###");
    }

    private void printNodeSummary(NodeBlockingStub node) {
        String nodeName = nodeName(node);

        log.info("#### {} summary ####", nodeName);
        ListpeersResponse listpeersResponse = node.listPeers(ListpeersRequest.newBuilder().build());
        log.info("  {} peers: {}", nodeName, listpeersResponse.getPeersList().stream()
                .map(it -> nodeNameByNodeId(it.getId()))
                .collect(Collectors.joining(", ")));

        ListpeerchannelsResponse peerChannels = listPeerChannels(node);
        log.info("  {} channel count: {}", nodeName, peerChannels.getChannelsCount());

        peerChannels.getChannelsList().forEach(it -> {
            boolean incoming = it.getOpener() == ChannelSide.REMOTE;
            log.info("  - {} channel ({}) {} {}--{} {} (state: {}, capacity: {}, spendable: {}, receivable: {}, peer_connected: {})",
                    incoming ? "Incoming" : "Outgoing",
                    it.getShortChannelId(), nodeName,
                    incoming ? "<" : "", incoming ? "" : ">",
                    nodeNameByNodeId(it.getPeerId()),
                    it.getState(),
                    new MilliSatoshi(it.getTotalMsat().getMsat()).truncateToSatoshi(),
                    new MilliSatoshi(it.getSpendableMsat().getMsat()),
                    new MilliSatoshi(it.getReceivableMsat().getMsat()),
                    it.getPeerConnected());
        });
    }

    private void afterChannelSetup() throws IOException {
        // mine a few more blocks to confirm the channels.
        // even if we do not need to mine any block, mine at least one just to confirm the nodes are syncing correctly!
        int minBlocks = 1;

        List<NodeBlockingStub> nodesWithChannelsAwaitingConfirmation = channelDefinitions.stream()
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
        waitForClnNodesBlockHeightSynchronization();
    }

    private void beforeChannelSetup() throws IOException {
        BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinClient);

        fundOnchainWallets(this.channelDefinitions);
    }

    private void fundOnchainWallets(List<ChannelDefinition> definitions) throws IOException {
        List<ChannelDefinition> missingChannels = definitions.stream()
                .filter(this::needsCreation)
                .toList();

        Map<NodeBlockingStub, Satoshi> nodesThatNeedFunding = missingChannels.stream()
                .collect(groupingBy(ChannelDefinition::getOrigin, mapping(
                        ChannelDefinition::getCapacity,
                        reducing(new Satoshi(0), Satoshi::plus)
                )));

        for (Map.Entry<NodeBlockingStub, Satoshi> entry : nodesThatNeedFunding.entrySet()) {
            NodeBlockingStub origin = entry.getKey();
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
            waitForClnNodesBlockHeightSynchronization();
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
                nodeName(definition.getOrigin()), definition.getCapacity(),
                nodeName(definition.getDestination()),
                needsChannelCreation ? "Created" : "Channel already present");
    }

    private boolean needsCreation(ChannelDefinition definition) {
        ListchannelsResponse outgoingChannels = listOutgoingChannels(definition.getOrigin());
        Optional<ListchannelsChannels> channelOrEmpty = outgoingChannels.getChannelsList().stream()
                .filter(it -> nodeIdHex(definition.getDestination()).equals(hex(it.getDestination())))
                .filter(it -> it.getAmountMsat().getMsat() == new MilliSatoshi(definition.getCapacity()).getMsat())
                .findFirst();

        return channelOrEmpty.isEmpty();
    }

    private void fundOnchainWallet(NodeBlockingStub target, int numBlocks) throws IOException {
        String address = target.newAddr(NewaddrRequest.newBuilder().build()).getBech32();
        log.debug("Will fund {}'s address {} with {} block reward(s)…", nodeName(target), address, numBlocks);
        bitcoinClient.generateToAddress(numBlocks, Address.fromString(RegTestParams.get(), address));
    }

    private void fundOnchainWallet(NodeBlockingStub target, Satoshi minAmount, int minUtxos) throws IOException {
        // TODO: For simplicity reasons, fund the wallet with ${minUtxos} blocks. _Might_ not be enough,
        //  but we should be safe as block rewards are 50 btc when the network initially starts.
        //  Refactor on demand!
        fundOnchainWallet(target, minUtxos);
    }

    private void createChannel(ChannelDefinition definition) {
        Satoshi onchainFunds = fetchOnchainFunds(definition.getOrigin());
        log.debug("{} controls on-chain funds amounting to {}", nodeName(definition.getOrigin()), onchainFunds);

        if (onchainFunds.getSat() <= definition.getCapacity().getSat()) {
            throw new IllegalStateException("Not enough funds: Cannot create channel of size " + definition.getCapacity());
        }
        FundchannelResponse fundchannelResponse = definition.getOrigin().fundChannel(FundchannelRequest.newBuilder()
                .setId(nodeIdBytes(definition.getDestination()))
                .setAmount(AmountOrAll.newBuilder()
                        .setAmount(Amount.newBuilder()
                                .setMsat(new MilliSatoshi(definition.getCapacity()).getMsat())
                                .build())
                        .build())
                .setPushMsat(Amount.newBuilder()
                        .setMsat(definition.getPushAmount().getMsat())
                        .build())
                .setAnnounce(definition.isAnnounced())
                .build());

        String channelId = hex(fundchannelResponse.getChannelId());
        log.debug("Created channel with capacity {}: {} (pushed {})", definition.getCapacity(), channelId, definition.getPushAmount());
    }

    private Satoshi fetchOnchainFunds(NodeBlockingStub node) {
        ListfundsResponse cln1ListfundsResponse = node.listFunds(ListfundsRequest.newBuilder()
                .setSpent(false)
                .build());

        return new MilliSatoshi(cln1ListfundsResponse.getOutputsList().stream()
                .mapToLong(it -> it.getAmountMsat().getMsat())
                .sum()).truncateToSatoshi();
    }

    private ListchannelsResponse listOutgoingChannels(NodeBlockingStub client) {
        return client.listChannels(ListchannelsRequest.newBuilder()
                .setSource(nodeIdBytes(client))
                .build());
    }

    private ListpeerchannelsResponse listPeerChannels(NodeBlockingStub client) {
        return client.listPeerChannels(ListpeerchannelsRequest.newBuilder()
                .build());
    }

    private void waitForClnNodesBlockHeightSynchronization() throws IOException {
        waitForClnNodeBlockHeightSynchronization(bitcoinClient, nodes());
    }

    // wait for cln nodes to catch up to the newest block height.
    // prevents `io.grpc.StatusRuntimeException: RpcError { code: Some(304), message: "Still syncing with bitcoin network" }`
    private static void waitForClnNodeBlockHeightSynchronization(BitcoinExtendedClient bitcoin, Collection<NodeBlockingStub> lnNodes) throws IOException {
        int currentBlockHeight = bitcoin.getBlockChainInfo().getBlocks();

        // does not need to be more often than every 5 seconds - cln can take quite long to synchronize
        Duration checkInterval = Duration.ofSeconds(5);
        // cln sometimes takes up to ~30 seconds when catching up to more than 100 blocks
        Duration timeout = Duration.ofSeconds(180);

        lnNodes.forEach(it -> waitForClnNodeBlockHeightSynchronization(it, currentBlockHeight, checkInterval, timeout));
    }

    private static void waitForClnNodeBlockHeightSynchronization(NodeBlockingStub client,
                                                                 int minBlockHeight,
                                                                 Duration checkInterval,
                                                                 Duration timeout) {
        Flux.interval(Duration.ZERO, checkInterval)
                .subscribeOn(Schedulers.newSingle("cln-wait-for-block-sync"))
                .map(it -> {
                    try {
                        GetinfoResponse getinfo = client.getinfo(GetinfoRequest.newBuilder().build());
                        boolean finished = getinfo.getBlockheight() >= minBlockHeight;

                        log.debug("Waiting for blockheight to reach {} on {}, currently at height {}: {}",
                                minBlockHeight, getinfo.getAlias(), getinfo.getBlockheight(),
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

}
