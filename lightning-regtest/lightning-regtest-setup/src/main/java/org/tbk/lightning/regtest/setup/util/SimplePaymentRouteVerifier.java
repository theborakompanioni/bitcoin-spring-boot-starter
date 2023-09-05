package org.tbk.lightning.regtest.setup.util;

import com.google.common.base.Stopwatch;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.Satoshi;
import fr.acinq.lightning.MilliSatoshi;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.CommonInfoRequest;
import org.tbk.lightning.client.common.core.proto.CommonInfoResponse;
import org.tbk.lightning.client.common.core.proto.CommonQueryRouteRequest;
import org.tbk.lightning.client.common.core.proto.CommonQueryRouteResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SimplePaymentRouteVerifier implements PaymentRouteVerifier {

    @Override
    public boolean hasDirectRoute(LightningCommonClient<?> origin, LightningCommonClient<?> destination) {
        CommonInfoResponse destInfo = destination.info(CommonInfoRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();

        return getRouteInternal(origin, destInfo.getIdentityPubkey())
                .map(it -> it.getRoutesCount() > 0)
                .onErrorComplete()
                .blockOptional(Duration.ofSeconds(30L))
                .orElse(false);
    }

    @Override
    public CommonQueryRouteResponse waitForRouteOrThrow(RouteVerification routeVerification) {
        CommonInfoResponse originInfo = routeVerification.getOrigin().getClient().info(CommonInfoRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();

        CommonInfoResponse destInfo = routeVerification.getDestination().getClient().info(CommonInfoRequest.newBuilder().build())
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow();

        log.debug("Waiting for a route from {} -> {} to become available…", originInfo.getAlias(), destInfo.getAlias());

        AtomicInteger tries = new AtomicInteger();
        Stopwatch sw = Stopwatch.createStarted();
        CommonQueryRouteResponse route = Flux.interval(Duration.ZERO, Duration.ofSeconds(1L))
                .doOnNext(it -> tries.incrementAndGet())
                .flatMap(it -> getRouteInternal(routeVerification.getOrigin().getClient(), destInfo.getIdentityPubkey()).onErrorComplete())
                .filter(it -> it.getRoutesCount() > 0)
                .blockFirst(routeVerification.getTimeout());

        if (route != null) {
            log.debug("Found route(s) from {} -> {} after {} tries and {}: {}",
                    originInfo.getAlias(), destInfo.getAlias(), tries.get(), sw.stop(), route);
        } else {
            String error = "Could not find a route from %s -> %s after %s.."
                    .formatted(originInfo.getAlias(), destInfo.getAlias(), routeVerification.getTimeout());
            throw new IllegalStateException(error);
        }

        return route;
    }

    private Mono<CommonQueryRouteResponse> getRouteInternal(LightningCommonClient<?> origin, ByteString destId) {
        // take 1 sat instead of 1 msat, as though CLN would allow it, it does not seem to work for LND nodes
        MilliSatoshi amount = new MilliSatoshi(new Satoshi(1));

        return origin.queryRoutes(CommonQueryRouteRequest.newBuilder()
                .setAmountMsat(amount.getMsat())
                .setRemoteIdentityPubkey(destId)
                .build());
    }
}
