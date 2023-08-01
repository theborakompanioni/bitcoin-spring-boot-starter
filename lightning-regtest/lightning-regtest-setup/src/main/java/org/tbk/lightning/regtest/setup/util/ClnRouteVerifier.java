package org.tbk.lightning.regtest.setup.util;

import org.tbk.lightning.client.common.core.LnCommonClient;
import org.tbk.lightning.cln.grpc.client.GetrouteResponse;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

/**
 * Verify routes exist between two lightning nodes.
 * @implNote Currently specific to CLN nodes, hence the prefix "Cln".
 */
public interface ClnRouteVerifier {
    boolean hasDirectRoute(LnCommonClient<NodeGrpc.NodeBlockingStub> origin, LnCommonClient<?> destination);

    GetrouteResponse waitForRouteOrThrow(RouteVerification routeVerification);
}
