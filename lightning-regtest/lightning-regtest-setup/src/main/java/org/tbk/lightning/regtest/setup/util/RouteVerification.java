package org.tbk.lightning.regtest.setup.util;

import fr.acinq.lightning.MilliSatoshi;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

import java.time.Duration;

@Value
@Builder
public class RouteVerification {
    private static final MilliSatoshi ZERO = new MilliSatoshi(0L);

    @NonNull
    NodeGrpc.NodeBlockingStub origin;

    @NonNull
    NodeGrpc.NodeBlockingStub destination;

    @Builder.Default
    Duration checkInterval = Duration.ofSeconds(2);

    /**
     * Default timeout to wait for route verification.
     * This is rather long, as for non-direct peers, this might take quite a while.
     * e.g.:
     * - Found a route from A -> D after 145 tries and 2.400 min
     * - Found a route from A -> C after 88 tries and 1.450 min
     */
    @Builder.Default
    Duration timeout = Duration.ofMinutes(5);

    /**
     * Configure whether the origin node should be connected to the destination node. Default is `true`.
     *
     * @implNote Connecting peers before verifying routes can decrease the time to find a route substantially,
     * as channel announcements gossiped will be received earlier. e.g.:
     * **without** connecting peers:
     * - Found route(s) from A -> Z after 147 tries and 2.434 min
     * - Found route(s) from A -> Z after 120 tries and 1.983 min
     * *with* connecting peers:
     * - Found route(s) from A -> Z after 62 tries and 1.017 min
     * - Found route(s) from A -> Z after 89 tries and 1.467 min
     */
    @Builder.Default
    boolean enableConnectingPeers = true;
}
