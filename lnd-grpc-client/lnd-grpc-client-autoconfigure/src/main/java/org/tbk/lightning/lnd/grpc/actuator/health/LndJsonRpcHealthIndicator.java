package org.tbk.lightning.lnd.grpc.actuator.health;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
public class LndJsonRpcHealthIndicator extends AbstractHealthIndicator {
    private final SynchronousLndAPI client;

    public LndJsonRpcHealthIndicator(SynchronousLndAPI client) {
        this.client = requireNonNull(client);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> baseDetails = ImmutableMap.<String, Object>builder()
                .put("performValidation", client.isPerformValidation())
                .build();

        try {
            builder.withDetails(baseDetails);
            doHealthCheckInternal(builder);
        } catch (Exception e) {
            log.error("Exception while performing lnd grpc client health check", e);

            builder.unknown()
                    .withException(e)
                    .withDetails(baseDetails);
        }
    }

    private void doHealthCheckInternal(Health.Builder builder) {
        log.debug("Performing health check with lnd grpc client");

        try {
            GetInfoResponse info = client.getInfo();
            builder.up().withDetails(ImmutableMap.<String, Object>builder()
                    .put("info", createMapFromInfoResponse(info))
                    .build());
        } catch (ValidationException e) {
            log.warn("Exception while performing health check with lnd grpc client: {}", e.getMessage());

            builder.unknown()
                    .withException(e)
                    .withDetails(ImmutableMap.<String, Object>builder()
                            .build());
        } catch (StatusException e) {
            log.warn("Exception while performing health check with lnd grpc client: {}", e.getMessage());

            builder.down()
                    .withException(e)
                    .withDetails(ImmutableMap.<String, Object>builder()
                            .put("status", e.getStatus())
                            .build());
        }
    }

    public static Map<String, Object> createMapFromInfoResponse(GetInfoResponse info) {
        return ImmutableMap.<String, Object>builder()
                .put("version", firstNonNull(info.getVersion(), "<empty>"))
                .put("commitHash", firstNonNull(info.getCommitHash(), "<empty>"))
                .put("identityPubkey", firstNonNull(info.getIdentityPubkey(), "<empty>"))
                .put("alias", firstNonNull(info.getAlias(), "<empty>"))
                .put("color", firstNonNull(info.getColor(), "<empty>"))
                .put("numPendingChannels", info.getNumPendingChannels())
                .put("numActiveChannels", info.getNumActiveChannels())
                .put("numInactiveChannels", info.getNumInactiveChannels())
                .put("numPeers", info.getNumPeers())
                .put("blockHeight", info.getBlockHeight())
                .put("blockHash", firstNonNull(info.getBlockHash(), "<empty>"))
                .put("bestHeaderTimestamp", info.getBestHeaderTimestamp())
                .put("syncedToChain", info.getSyncedToChain())
                .put("syncedToGraph", info.getSyncedToGraph())
                .put("testnet", info.getTestnet())
                .put("featuresEntries", info.getFeaturesEntries().getEntry().stream()
                        .map(it -> ImmutableMap.<String, Object>builder()
                                .put("key", it.getKey())
                                .put("name", firstNonNull(it.getValue().getName(), "<empty>"))
                                .put("isKnown", it.getValue().getIsKnown())
                                .put("isRequired", it.getValue().getIsRequired())
                                .build())
                        .collect(Collectors.toList()))
                .put("chains", Optional.of(info).map(it -> {
                    try {
                        return it.getChains();
                    } catch (ClientSideException e) {
                        log.warn("Exception occurred while constructing chains: {}", e.getMessage());
                        return Collections.<Chain>emptyList();
                    }
                }).stream()
                        .flatMap(Collection::stream)
                        .map(it -> ImmutableMap.<String, Object>builder()
                                .put("network", firstNonNull(it.getNetwork(), "<empty>"))
                                .put("chain", firstNonNull(it.getChain(), "<empty>"))
                                .build())
                        .collect(Collectors.toList()))
                .put("uris", Optional.of(info).map(it -> {
                    try {
                        return it.getUris();
                    } catch (ClientSideException e) {
                        log.warn("Exception occurred while constructing uris: {}", e.getMessage());
                        return Collections.<String>emptyList();
                    }
                }).stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .build();
    }
}
