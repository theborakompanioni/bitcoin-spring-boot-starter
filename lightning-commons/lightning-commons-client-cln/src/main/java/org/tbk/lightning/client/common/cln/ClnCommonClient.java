package org.tbk.lightning.client.common.cln;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.client.common.core.proto.CommonLookupInvoiceResponse.InvoiceStatus;
import org.tbk.lightning.client.common.core.proto.CommonPayResponse.PaymentStatus;
import org.tbk.lightning.cln.grpc.client.*;
import reactor.core.publisher.Mono;

import java.util.HexFormat;
import java.util.List;

/**
 * See: <a href="https://docs.corelightning.org/reference">CLN API Docs</a>
 */
@Slf4j
@RequiredArgsConstructor
public class ClnCommonClient implements LightningCommonClient {

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
                    .setNumPendingChannels(response.getNumPendingChannels())
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
            ConnectRequest.Builder builder = ConnectRequest.newBuilder()
                    .setId(HexFormat.of().formatHex(request.getIdentityPubkey().toByteArray()));

            if (request.hasHost()) {
                builder.setHost(request.getHost());
            }
            if (request.hasPort()) {
                builder.setPort(request.getPort());
            }

            ConnectResponse connectResponse = client.connectPeer(builder.build());

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
    public Mono<CommonListUnspentResponse> listUnspent(CommonListUnspentRequest request) {
        return Mono.fromCallable(() -> {
            ListfundsResponse response = client.listFunds(ListfundsRequest.newBuilder()
                    .setSpent(false)
                    .build());

            List<UnspentOutput> unspentOutputs = response.getOutputsList().stream()
                    .map(it -> UnspentOutput.newBuilder()
                            .setTxid(it.getTxid())
                            .setOutputIndex(it.getOutput())
                            .setAmountMsat(it.getAmountMsat().getMsat())
                            .setScriptPubkey(it.getScriptpubkey())
                            .build())
                    .toList();

            return CommonListUnspentResponse.newBuilder()
                    .addAllUnspentOutputs(unspentOutputs)
                    .build();
        });
    }

    @Override
    public Mono<CommonListPeerChannelsResponse> listPeerChannels(CommonListPeerChannelsRequest request) {
        return Mono.fromCallable(() -> {
            ListpeerchannelsResponse response = client.listPeerChannels(ListpeerchannelsRequest.newBuilder().build());

            List<PeerChannel> outgoingChannels = response.getChannelsList().stream()
                    .map(it -> {
                        PeerChannel.Builder builder = PeerChannel.newBuilder()
                                .setRemoteIdentityPubkey(it.getPeerId())
                                .setCapacityMsat(it.getTotalMsat().getMsat())
                                .setAnnounced(!it.getPrivate())
                                .setActive(it.getState() == ListpeerchannelsChannels.ListpeerchannelsChannelsState.CHANNELD_NORMAL)
                                .setInitiator(it.getOpener() == ChannelSide.LOCAL);

                        if (it.hasToUsMsat()) {
                            builder.setLocalBalanceMsat(it.getToUsMsat().getMsat());
                            builder.setRemoteBalanceMsat(it.getTotalMsat().getMsat() - it.getToUsMsat().getMsat());
                        }
                        if (it.hasSpendableMsat()) {
                            builder.setEstimatedSpendableMsat(it.getSpendableMsat().getMsat());
                        }
                        if (it.hasReceivableMsat()) {
                            builder.setEstimatedReceivableMsat(it.getReceivableMsat().getMsat());
                        }
                        return builder.build();
                    })
                    .toList();

            return CommonListPeerChannelsResponse.newBuilder()
                    .addAllPeerChannels(outgoingChannels)
                    .build();
        });
    }

    @Override
    public Mono<CommonPayResponse> pay(CommonPayRequest request) {
        return Mono.fromCallable(() -> {
            PayRequest.Builder builder = PayRequest.newBuilder()
                    .setBolt11(request.getPaymentRequest());

            if (request.hasAmountMsat()) {
                builder.setAmountMsat(Amount.newBuilder()
                        .setMsat(request.getAmountMsat())
                        .build());
            }
            if (request.hasTimeoutSeconds()) {
                builder.setRetryFor(request.getTimeoutSeconds());
            }
            if (request.hasMaxFeeMsat()) {
                builder.setMaxfee(Amount.newBuilder()
                        .setMsat(request.getMaxFeeMsat())
                        .build());
            }

            PayResponse response = client.pay(builder.build());

            PaymentStatus status = switch (response.getStatus()) {
                case COMPLETE -> PaymentStatus.COMPLETE;
                case PENDING -> PaymentStatus.PENDING;
                case FAILED -> PaymentStatus.FAILED;
                case UNRECOGNIZED -> PaymentStatus.UNKNOWN;
            };

            return CommonPayResponse.newBuilder()
                    .setPaymentHash(response.getPaymentHash())
                    .setStatus(status)
                    .setAmountMsat(response.getAmountMsat().getMsat())
                    .setPaymentPreimage(response.getPaymentPreimage())
                    .build();
        });
    }

    @Override
    public Mono<CommonLookupInvoiceResponse> lookupInvoice(CommonLookupInvoiceRequest request) {
        return Mono.fromCallable(() -> {
            ListinvoicesResponse response = client.listInvoices(ListinvoicesRequest.newBuilder()
                    .setPaymentHash(request.getPaymentHash())
                    .build());

            if (response.getInvoicesCount() <= 0) {
                return null; // results in empty mono
            }

            ListinvoicesInvoices invoice = response.getInvoices(response.getInvoicesCount() - 1);

            InvoiceStatus status = switch (invoice.getStatus()) {
                case UNPAID -> InvoiceStatus.PENDING;
                case PAID -> InvoiceStatus.COMPLETE;
                case EXPIRED -> InvoiceStatus.CANCELLED;
                case UNRECOGNIZED -> InvoiceStatus.UNKNOWN;
            };

            return CommonLookupInvoiceResponse.newBuilder()
                    .setPaymentHash(invoice.getPaymentHash())
                    .setPaymentPreimage(invoice.getPaymentPreimage())
                    .setAmountMsat(invoice.getAmountMsat().getMsat())
                    .setStatus(status)
                    .build();
        });
    }

    @Override
    public Mono<CommonLookupPaymentResponse> lookupPayment(CommonLookupPaymentRequest request) {
        return Mono.fromCallable(() -> {
            ListpaysResponse response = client.listPays(ListpaysRequest.newBuilder()
                    .setPaymentHash(request.getPaymentHash())
                    .build());

            if (response.getPaysCount() <= 0) {
                return null;
            }

            ListpaysPays payment = response.getPays(response.getPaysCount() - 1);

            PaymentStatus status = switch (payment.getStatus()) {
                case COMPLETE -> PaymentStatus.COMPLETE;
                case PENDING -> PaymentStatus.PENDING;
                case FAILED -> PaymentStatus.FAILED;
                case UNRECOGNIZED -> PaymentStatus.UNKNOWN;
            };

            CommonLookupPaymentResponse.Builder responseBuilder = CommonLookupPaymentResponse.newBuilder()
                    .setPaymentHash(payment.getPaymentHash())
                    .setStatus(status);

            if (payment.hasPreimage()) {
                responseBuilder.setPaymentPreimage(payment.getPreimage());
            }

            // TODO: next release will contain the amount
            // if (payment.hasAmount()) {
            //     responseBuilder.setAmountMsat(payment.getAmount().getMsat())
            // }

            return responseBuilder.build();
        });
    }

    @Override
    public Mono<CommonQueryRouteResponse> queryRoutes(CommonQueryRouteRequest request) {
        return Mono.fromCallable(() -> {
            GetrouteResponse response = this.client.getRoute(GetrouteRequest.newBuilder()
                    .setAmountMsat(Amount.newBuilder().setMsat(request.getAmountMsat()).build())
                    .setId(request.getRemoteIdentityPubkey())
                    .setRiskfactor(0) // we are just interested if a route exists
                    .setFuzzpercent(0)
                    .build());

            if (response.getRouteList().isEmpty()) {
                return CommonQueryRouteResponse.newBuilder().build();
            }

            // cln `getroute` response represents a single route
            CommonQueryRouteResponse.Route route = CommonQueryRouteResponse.Route.newBuilder()
                    .addAllHops(response.getRouteList().stream()
                            .map(hop -> CommonQueryRouteResponse.Hop.newBuilder()
                                    .setIdentityPubkey(hop.getId())
                                    .setAmountMsat(hop.getAmountMsat().getMsat())
                                    .build())
                            .toList())
                    .build();

            return CommonQueryRouteResponse.newBuilder()
                    .addRoutes(route)
                    .build();
        });
    }
}
