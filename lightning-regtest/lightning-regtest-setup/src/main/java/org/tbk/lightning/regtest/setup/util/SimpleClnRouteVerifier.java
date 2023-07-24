package org.tbk.lightning.regtest.setup.util;

import com.google.common.base.Stopwatch;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lightning.cln.grpc.client.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SimpleClnRouteVerifier implements ClnRouteVerifier {
    private static final Amount MILLISATOSHI = Amount.newBuilder().setMsat(1L).build();

    @Override
    public boolean hasDirectRoute(NodeGrpc.NodeBlockingStub origin, NodeGrpc.NodeBlockingStub destination) {
        GetinfoResponse destInfo = destination.getinfo(GetinfoRequest.newBuilder().build());
        return getRouteInternal(origin, destInfo.getId())
                .map(it -> it.getRouteCount() > 0)
                .orElse(false);
    }

    @Override
    public GetrouteResponse waitForRouteOrThrow(RouteVerification routeVerification) {
        GetinfoResponse originInfo = routeVerification.getOrigin().getinfo(GetinfoRequest.newBuilder().build());
        GetinfoResponse destInfo = routeVerification.getDestination().getinfo(GetinfoRequest.newBuilder().build());

        log.debug("Waiting for a route from {} -> {} to become available…", originInfo.getAlias(), destInfo.getAlias());

        AtomicInteger tries = new AtomicInteger();
        Stopwatch sw = Stopwatch.createStarted();
        Duration waitForRouteTimeout = Duration.ofMinutes(15);
        GetrouteResponse route = Flux.interval(Duration.ZERO, Duration.ofSeconds(1L))
                .doOnNext(it -> tries.incrementAndGet())
                .map(it -> getRouteInternal(routeVerification.getOrigin(), destInfo.getId()))
                .mapNotNull(it -> it.orElse(null))
                .filter(it -> it.getRouteCount() > 0)
                .blockFirst(waitForRouteTimeout);

        if (route != null) {
            log.debug("Found route(s) from {} -> {} after {} tries and {}: {}",
                    originInfo.getAlias(), destInfo.getAlias(), tries.get(), sw.stop(), route);
        } else {
            String error = "Could not find a route from %s -> %s after %s.."
                    .formatted(originInfo.getAlias(), destInfo.getAlias(), waitForRouteTimeout);
            throw new IllegalStateException(error);
        }

        return route;
    }

    private Optional<GetrouteResponse> getRouteInternal(NodeGrpc.NodeBlockingStub origin, ByteString destId) {
        try {
            // see https://lightning.readthedocs.io/lightning-getroute.7.html
            return Optional.ofNullable(origin.getRoute(GetrouteRequest.newBuilder()
                    .setAmountMsat(MILLISATOSHI)
                    .setId(destId)
                    .setRiskfactor(0) // we are just interested if a route exists
                    .setFuzzpercent(0)
                    .build()));
        } catch (Exception e) {
            log.trace("Checking route - ignore exception on purpose: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
