package org.tbk.lightning.client.common.cln;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.cln.grpc.client.*;
import reactor.core.publisher.Mono;

import java.util.HexFormat;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ClnCommonClient implements LightningCommonClient<NodeGrpc.NodeBlockingStub> {

    @NonNull
    private final NodeGrpc.NodeBlockingStub client;

    @Override
    public Mono<CommonInfoResponse> info(CommonInfoRequest request) {
        return Mono.fromCallable(() -> {
            GetinfoResponse response = client.getinfo(GetinfoRequest.newBuilder().build());
            return CommonInfoResponse.newBuilder()
                    .setIdentityPubkey(response.getId())
                    .setAlias(response.getAlias())
                    .setColor(response.getColor())
                    .setNumPeers(response.getNumPeers())
                    .setNumPendingChannels(response.getNumActiveChannels())
                    .setNumActiveChannels(response.getNumActiveChannels())
                    .setNumInactiveChannels(response.getNumInactiveChannels())
                    .setVersion(response.getVersion())
                    .addChain(Chain.newBuilder()
                            .setChain("bitcoin")
                            .setNetwork(response.getNetwork())
                            .build())
                    .setBlockheight(response.getBlockheight())
                    .build();
        });
    }

    @Override
    public Mono<CommonConnectResponse> connect(CommonConnectRequest request) {
        return Mono.fromCallable(() -> {
            org.tbk.lightning.cln.grpc.client.ConnectRequest.Builder builder = org.tbk.lightning.cln.grpc.client.ConnectRequest.newBuilder()
                    .setId(HexFormat.of().formatHex(request.getIdentityPubkey().toByteArray()));

            if (request.hasHost()) {
                builder.setHost(request.getHost());
            }
            if (request.hasPort()) {
                builder.setPort(request.getPort());
            }

            org.tbk.lightning.cln.grpc.client.ConnectResponse connectResponse = client.connectPeer(builder.build());

            log.trace("'connectPeer' returned': {}", connectResponse);

            return CommonConnectResponse.newBuilder().build();
        });
    }

    @Override
    public Mono<CommonNewAddressResponse> newAddress(CommonNewAddressRequest request) {
        return Mono.fromCallable(() -> {
            NewaddrResponse response = client.newAddr(NewaddrRequest.newBuilder().build());

            return CommonNewAddressResponse.newBuilder()
                    .setAddress(response.getBech32())
                    .build();
        });
    }

    @Override
    public Mono<CommonCreateInvoiceResponse> createInvoice(CommonCreateInvoiceRequest request) {
        return Mono.fromCallable(() -> {
            AmountOrAny amountOrAny = request.hasAmountMsat() ? AmountOrAny.newBuilder()
                    .setAmount(Amount.newBuilder()
                            .setMsat(request.getAmountMsat())
                            .build())
                    .build() : AmountOrAny.newBuilder()
                    .setAny(true)
                    .build();

            InvoiceRequest.Builder builder = InvoiceRequest.newBuilder()
                    .setAmountMsat(amountOrAny)
                    .setLabel(request.getLabel());

            if (request.hasDescription()) {
                builder.setDescription(request.getDescription());
            }
            if (request.hasExpiry()) {
                builder.setExpiry(request.getExpiry());
            }

            InvoiceResponse response = client.invoice(builder.build());

            return CommonCreateInvoiceResponse.newBuilder()
                    .setPaymentRequest(response.getBolt11())
                    .setPaymentHash(response.getPaymentHash())
                    .build();
        });
    }

    @Override
    public Mono<CommonListPeersResponse> listPeers(CommonListPeersRequest request) {
        return Mono.fromCallable(() -> {
            ListpeersResponse response = client.listPeers(ListpeersRequest.newBuilder().build());

            List<Peer> peers = response.getPeersList().stream()
                    .map(it -> Peer.newBuilder()
                            .setIdentityPubkey(it.getId())
                            .addAllNetworkAddresses(it.getNetaddrList())
                            .build())
                    .toList();

            return CommonListPeersResponse.newBuilder()
                    .addAllPeers(peers)
                    .build();
        });
    }

    @Override
    public Mono<CommonOpenChannelResponse> openChannel(CommonOpenChannelRequest request) {
        return Mono.fromCallable(() -> {
            FundchannelRequest.Builder builder = FundchannelRequest.newBuilder()
                    .setId(request.getIdentityPubkey())
                    .setAmount(AmountOrAll.newBuilder()
                            .setAmount(Amount.newBuilder()
                                    .setMsat(request.getAmountMsat())
                                    .build())
                            .build())
                    .setPushMsat(Amount.newBuilder()
                            .setMsat(request.hasPushMsat() ? request.getPushMsat() : 0)
                            .build())
                    .setAnnounce(!request.hasAnnounce() || request.getAnnounce());

            if (request.hasSatPerVbyte()) {
                builder.setFeerate(Feerate.newBuilder()
                        .setPerkb(request.getSatPerVbyte() * 1_000)
                        .build());
            }
            if (request.hasTargetConf()) {
                builder.setMinconf(request.getTargetConf());
            }
            if (request.hasMinUtxoDepth()) {
                builder.setMindepth(request.getMinUtxoDepth());
            }
            if (request.hasCloseToAddress()) {
                builder.setCloseTo(request.getCloseToAddress());
            }

            FundchannelResponse response = client.fundChannel(builder.build());

            return CommonOpenChannelResponse.newBuilder()
                    .setTxid(response.getTxid())
                    .setOutputIndex(response.getOutnum())
                    .build();
        });
    }

    @Override
    public NodeGrpc.NodeBlockingStub baseClient() {
        return client;
    }
}
