package org.tbk.lightning.client.common.lnd;

import com.google.protobuf.ByteString;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.message.GetInfoRequest;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.tbk.lightning.client.common.core.LnCommonClient;
import org.tbk.lightning.client.common.core.proto.Chain;
import org.tbk.lightning.client.common.core.proto.GetinfoRequest;
import org.tbk.lightning.client.common.core.proto.GetinfoResponse;
import reactor.core.publisher.Mono;

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
    public SynchronousLndAPI baseClient() {
        return client;
    }
}
