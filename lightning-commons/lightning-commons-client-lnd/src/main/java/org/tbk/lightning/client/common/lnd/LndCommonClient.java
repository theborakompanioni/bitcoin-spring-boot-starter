package org.tbk.lightning.client.common.lnd;

import com.google.protobuf.ByteString;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.invoices.proto.InvoicesOuterClass;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.router.proto.RouterOuterClass;
import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.invoices.SynchronousInvoicesAPI;
import org.lightningj.lnd.wrapper.invoices.message.LookupInvoiceMsg;
import org.lightningj.lnd.wrapper.message.*;
import org.lightningj.lnd.wrapper.router.SynchronousRouterAPI;
import org.lightningj.lnd.wrapper.router.message.SendPaymentRequest;
import org.lightningj.lnd.wrapper.router.message.TrackPaymentRequest;
import org.lightningj.lnd.wrapper.walletkit.SynchronousWalletKitAPI;
import org.lightningj.lnd.wrapper.walletkit.message.AddrRequest;
import org.lightningj.lnd.wrapper.walletkit.message.AddrResponse;
import org.lightningj.lnd.wrapper.walletkit.message.ListUnspentRequest;
import org.lightningj.lnd.wrapper.walletkit.message.ListUnspentResponse;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.Chain;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.client.common.core.proto.CommonLookupInvoiceResponse.InvoiceStatus;
import org.tbk.lightning.client.common.core.proto.Peer;
import org.tbk.lightning.client.common.core.proto.CommonPayResponse.PaymentStatus;
import reactor.core.publisher.Mono;

import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * See: <a href="https://lightning.engineering/api-docs/api/lnd/">LND API Docs</a>
 */
@Slf4j
@RequiredArgsConstructor
public class LndCommonClient implements LightningCommonClient<SynchronousLndAPI> {

    @NonNull
    private final SynchronousLndAPI client;

    @NonNull
    private final SynchronousWalletKitAPI walletKitApi;

    @NonNull
    private final SynchronousRouterAPI routerApi;

    @NonNull
    private final SynchronousInvoicesAPI invoicesApi;

    @Override
    public Mono<CommonInfoResponse> info(CommonInfoRequest request) {
        return Mono.fromCallable(() -> {
            GetInfoResponse response = client.getInfo(new GetInfoRequest(LightningApi.GetInfoRequest.newBuilder().build()));
            return CommonInfoResponse.newBuilder()
                    .setIdentityPubkey(ByteString.fromHex(response.getIdentityPubkey()))
                    .setAlias(response.getAlias())
                    .setColor(ByteString.fromHex(response.getColor()))
                    .setNumPeers(response.getNumPeers())
                    .setNumPendingChannels(response.getNumPendingChannels())
                    .setNumActiveChannels(response.getNumActiveChannels())
                    .setNumInactiveChannels(response.getNumInactiveChannels())
                    .setVersion(response.getVersion())
                    .addAllChain(response.getChains().stream()
                            .map(it -> Chain.newBuilder()
                                    .setChain(it.getChain())
                                    .setNetwork(it.getNetwork())
                                    .build())
                            .toList())
                    .setBlockheight(response.getBlockHeight())
                    .build();
        });
    }

    @Override
    public Mono<CommonConnectResponse> connect(CommonConnectRequest request) {
        return Mono.fromCallable(() -> {
            LightningApi.LightningAddress.Builder addressBuilder = LightningApi.LightningAddress.newBuilder()
                    .setPubkey(HexFormat.of().formatHex(request.getIdentityPubkey().toByteArray()));

            Optional<String> hostOrEmpty = Optional.of(request.getHost())
                    .filter(foo -> request.hasHost())
                    .filter(it -> !it.isBlank())
                    .map(it -> request.hasPort() ? "%s:%d".formatted(it, request.getPort()) : it);

            hostOrEmpty.ifPresent(addressBuilder::setHost);

            ConnectPeerResponse connectPeerResponse = client.connectPeer(new ConnectPeerRequest(LightningApi.ConnectPeerRequest.newBuilder()
                    .setAddr(addressBuilder.build())
                    .build()));

            log.trace("'connectPeer' returned': {}", connectPeerResponse);

            return CommonConnectResponse.newBuilder().build();
        });
    }

    @Override
    public Mono<CommonNewAddressResponse> newAddress(CommonNewAddressRequest request) {
        return Mono.fromCallable(() -> {
            AddrResponse response = walletKitApi.nextAddr(new AddrRequest());
            return CommonNewAddressResponse.newBuilder()
                    .setAddress(response.getAddr())
                    .build();
        });
    }

    @Override
    public Mono<CommonCreateInvoiceResponse> createInvoice(CommonCreateInvoiceRequest request) {
        return Mono.fromCallable(() -> {
            LightningApi.Invoice.Builder builder = LightningApi.Invoice.newBuilder();

            if (request.hasAmountMsat()) {
                builder.setValueMsat(request.getAmountMsat());
            }
            if (request.hasDescription()) {
                builder.setMemo(request.getDescription());
            }
            if (request.hasExpiry()) {
                builder.setExpiry(request.getExpiry());
            }

            AddInvoiceResponse response = client.addInvoice(new Invoice(builder.build()));

            return CommonCreateInvoiceResponse.newBuilder()
                    .setPaymentRequest(response.getPaymentRequest())
                    .setPaymentHash(ByteString.copyFrom(response.getRHash()))
                    .build();
        });
    }

    @Override
    public Mono<CommonListPeersResponse> listPeers(CommonListPeersRequest request) {
        return Mono.fromCallable(() -> {
            ListPeersResponse response = client.listPeers(new ListPeersRequest());

            List<Peer> peers = response.getPeers().stream()
                    .map(it -> Peer.newBuilder()
                            .setIdentityPubkey(ByteString.fromHex(it.getPubKey()))
                            .addNetworkAddresses(it.getAddress())
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
            LightningApi.OpenChannelRequest.Builder builder = LightningApi.OpenChannelRequest.newBuilder()
                    .setNodePubkey(request.getIdentityPubkey())
                    .setLocalFundingAmount(request.getAmountMsat())
                    .setPushSat(request.hasPushMsat() ? request.getPushMsat() : 0)
                    .setPrivate(request.hasAnnounce() && !request.getAnnounce());

            if (request.hasSatPerVbyte()) {
                builder.setSatPerVbyte(request.getSatPerVbyte());
            }
            if (request.hasTargetConf()) {
                builder.setTargetConf(request.getTargetConf());
            }
            if (request.hasMinUtxoDepth()) {
                builder.setMinConfs(request.getMinUtxoDepth());
            }
            if (request.hasCloseToAddress()) {
                builder.setCloseAddress(request.getCloseToAddress());
            }

            ChannelPoint response = client.openChannelSync(new OpenChannelRequest());

            byte[] txid = Optional.ofNullable(response.getFundingTxidBytes())
                    .orElseGet(() -> HexFormat.of().parseHex(response.getFundingTxidStr()));

            return CommonOpenChannelResponse.newBuilder()
                    .setTxid(ByteString.copyFrom(txid))
                    .setOutputIndex(response.getOutputIndex())
                    .build();
        });
    }

    @Override
    public Mono<CommonListUnspentResponse> listUnspent(CommonListUnspentRequest request) {
        return Mono.fromCallable(() -> {
            ListUnspentResponse response = walletKitApi.listUnspent(new ListUnspentRequest());

            List<UnspentOutput> unspentOutputs = response.getUtxos().stream()
                    .map(it -> {
                        try {
                            OutPoint outpoint = it.getOutpoint();

                            byte[] txid = Optional.ofNullable(outpoint.getTxidBytes())
                                    .orElseGet(() -> HexFormat.of().parseHex(outpoint.getTxidStr()));

                            return UnspentOutput.newBuilder()
                                    .setTxid(ByteString.copyFrom(txid))
                                    .setOutputIndex(outpoint.getOutputIndex())
                                    .setAmountMsat(it.getAmountSat() * 1_000)
                                    .setScriptPubkey(ByteString.fromHex(it.getPkScript()))
                                    .build();
                        } catch (ClientSideException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            return CommonListUnspentResponse.newBuilder()
                    .addAllUnspentOutputs(unspentOutputs)
                    .build();
        });
    }

    @Override
    public Mono<CommonListPeerChannelsResponse> listPeerChannels(CommonListPeerChannelsRequest request) {
        return Mono.fromCallable(() -> {
            ListChannelsResponse response = client.listChannels(new ListChannelsRequest());

            List<PeerChannel> outgoingChannels = response.getChannels().stream()
                    .map(it -> {
                        try {
                            long localChannelReserveMsat = it.getLocalConstraints().getChanReserveSat() * 1_000;
                            long spendable = Math.max(it.getLocalBalance(), localChannelReserveMsat) - localChannelReserveMsat;

                            long remoteChannelReserveMsat = it.getRemoteConstraints().getChanReserveSat() * 1_000;
                            long receivable = Math.max(it.getRemoteBalance(), remoteChannelReserveMsat) - remoteChannelReserveMsat;

                            return PeerChannel.newBuilder()
                                    .setRemoteIdentityPubkey(ByteString.fromHex(it.getRemotePubkey()))
                                    .setCapacityMsat(it.getCapacity())
                                    .setAnnounced(!it.getPrivate())
                                    .setActive(it.getActive())
                                    .setInitiator(it.getInitiator())
                                    .setLocalBalanceMsat(it.getLocalBalance())
                                    .setRemoteBalanceMsat(it.getRemoteBalance())
                                    .setEstimatedSpendableMsat(spendable)
                                    .setEstimatedReceivableMsat(receivable)
                                    .build();
                        } catch (ClientSideException e) {
                            throw new RuntimeException(e);
                        }
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
            RouterOuterClass.SendPaymentRequest.Builder builder = RouterOuterClass.SendPaymentRequest.newBuilder()
                    .setPaymentRequest(request.getPaymentRequest());

            if (request.hasAmountMsat()) {
                builder.setAmtMsat(request.getAmountMsat());
            }
            if (request.hasTimeoutSeconds()) {
                builder.setTimeoutSeconds(request.getTimeoutSeconds());
            }
            if (request.hasMaxFeeMsat()) {
                builder.setFeeLimitMsat(request.getMaxFeeMsat());
            }

            Iterator<Payment> paymentIterator = routerApi.sendPaymentV2(new SendPaymentRequest(builder.build()));
            Payment payment = last(paymentIterator);
            if (payment == null) {
                return null;
            }

            PaymentStatus status = switch (payment.getStatus()) {
                case SUCCEEDED -> PaymentStatus.COMPLETE;
                case IN_FLIGHT -> PaymentStatus.PENDING;
                case FAILED -> PaymentStatus.FAILED;
                case UNKNOWN -> PaymentStatus.UNKNOWN;
            };

            return CommonPayResponse.newBuilder()
                    .setPaymentHash(ByteString.fromHex(payment.getPaymentHash()))
                    .setStatus(status)
                    .setAmountMsat(payment.getValueMsat())
                    .setPaymentPreimage(Optional.ofNullable(payment.getPaymentPreimage())
                            .map(ByteString::fromHex)
                            .orElse(ByteString.EMPTY))
                    .build();
        });
    }

    @Override
    public Mono<CommonLookupPaymentResponse> lookupPayment(CommonLookupPaymentRequest request) {
        return Mono.fromCallable(() -> {

            Iterator<Payment> paymentIterator = routerApi.trackPaymentV2(new TrackPaymentRequest(RouterOuterClass.TrackPaymentRequest.newBuilder()
                    .setPaymentHash(request.getPaymentHash())
                    .build()));

            Payment payment = last(paymentIterator);
            if (payment == null) {
                return null;
            }

            PaymentStatus status = switch (payment.getStatus()) {
                case UNKNOWN -> PaymentStatus.UNKNOWN;
                case IN_FLIGHT -> PaymentStatus.PENDING;
                case SUCCEEDED -> PaymentStatus.COMPLETE;
                case FAILED -> PaymentStatus.FAILED;
            };

            CommonLookupPaymentResponse.Builder responseBuilder = CommonLookupPaymentResponse.newBuilder()
                    .setPaymentHash(ByteString.fromHex(payment.getPaymentHash()))
                    .setAmountMsat(payment.getValueMsat())
                    .setStatus(status);

            Optional.ofNullable(payment.getPaymentPreimage())
                    .filter(it -> !it.isBlank())
                    .map(ByteString::fromHex)
                    .ifPresent(responseBuilder::setPaymentPreimage);

            return responseBuilder.build();
        });
    }

    @Override
    public Mono<CommonLookupInvoiceResponse> lookupInvoice(CommonLookupInvoiceRequest request) {
        return Mono.fromCallable(() -> {
            Invoice invoice = invoicesApi.lookupInvoiceV2(new LookupInvoiceMsg(InvoicesOuterClass.LookupInvoiceMsg.newBuilder()
                    .setPaymentHash(request.getPaymentHash())
                    .build()));

            InvoiceStatus status = switch (invoice.getState()) {
                case OPEN, ACCEPTED -> InvoiceStatus.PENDING;
                case SETTLED -> InvoiceStatus.COMPLETE;
                case CANCELED -> InvoiceStatus.CANCELLED;
            };

            return CommonLookupInvoiceResponse.newBuilder()
                    .setPaymentHash(ByteString.copyFrom(invoice.getRHash()))
                    .setPaymentPreimage(ByteString.copyFrom(invoice.getRPreimage()))
                    .setAmountMsat(invoice.getValueMsat())
                    .setStatus(status)
                    .build();
        });
    }

    @Override
    public SynchronousLndAPI baseClient() {
        return client;
    }

    private static <T> T last(Iterator<T> iterator) {
        while (true) {
            T current = iterator.next();
            if (!iterator.hasNext()) {
                return current;
            }
        }
    }
}
