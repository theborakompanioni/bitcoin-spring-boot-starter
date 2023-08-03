package org.tbk.lightning.client.common.cln;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lightning.client.common.core.LnCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import reactor.core.publisher.Mono;

import java.util.HexFormat;

@Slf4j
@RequiredArgsConstructor
public class ClnCommonClient implements LnCommonClient<NodeGrpc.NodeBlockingStub> {

    @NonNull
    private final NodeGrpc.NodeBlockingStub client;

    @Override
    public Mono<GetinfoResponse> info(GetinfoRequest request) {
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

    @Override
    public Mono<ConnectResponse> connect(ConnectRequest request) {
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

            return ConnectResponse.newBuilder().build();
        });
    }

    @Override
    public NodeGrpc.NodeBlockingStub baseClient() {
        return client;
    }
}
