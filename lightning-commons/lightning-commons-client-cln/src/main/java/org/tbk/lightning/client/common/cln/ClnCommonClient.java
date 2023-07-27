package org.tbk.lightning.client.common.cln;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.lightning.client.common.core.LnCommonClient;
import org.tbk.lightning.client.common.core.proto.Chain;
import org.tbk.lightning.client.common.core.proto.GetinfoRequest;
import org.tbk.lightning.client.common.core.proto.GetinfoResponse;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ClnCommonClient implements LnCommonClient {

    @NonNull
    private final NodeGrpc.NodeBlockingStub client;

    @Override
    public Mono<GetinfoResponse> getInfo(GetinfoRequest request) {
        return Mono.fromCallable(() -> {
            org.tbk.lightning.cln.grpc.client.GetinfoResponse response = client.getinfo(org.tbk.lightning.cln.grpc.client.GetinfoRequest.newBuilder().build());
            return GetinfoResponse.newBuilder()
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
}
