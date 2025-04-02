package org.tbk.lightning.client.common.lnd;

import com.google.protobuf.ByteString;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.message.ConnectPeerRequest;
import org.lightningj.lnd.wrapper.message.ConnectPeerResponse;
import org.lightningj.lnd.wrapper.message.GetInfoRequest;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.tbk.lightning.client.common.core.LnCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import reactor.core.publisher.Mono;

import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LndCommonClient implements LnCommonClient<SynchronousLndAPI> {

    @NonNull
    private final SynchronousLndAPI client;

    @Override
    public Mono<GetinfoResponse> info(GetinfoRequest request) {
        return Mono.fromCallable(() -> {
            GetInfoResponse response = client.getInfo(new GetInfoRequest(LightningApi.GetInfoRequest.newBuilder().build()));
            return GetinfoResponse.newBuilder()
                    .setIdentityPubkey(ByteString.fromHex(response.getIdentityPubkey()))
                    .setAlias(response.getAlias())
                    .setColor(ByteString.fromHex(response.getColor()))
                    .setNumPeers(response.getNumPeers())
                    .setNumPendingChannels(response.getNumActiveChannels())
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
    public Mono<ConnectResponse> connect(ConnectRequest request) {
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

            return ConnectResponse.newBuilder().build();
        });
    }

    @Override
    public SynchronousLndAPI baseClient() {
        return client;
    }
}
