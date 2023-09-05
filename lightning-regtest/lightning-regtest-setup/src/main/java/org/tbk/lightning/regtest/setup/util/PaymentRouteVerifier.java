package org.tbk.lightning.regtest.setup.util;

import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.CommonQueryRouteResponse;

public interface PaymentRouteVerifier {
    boolean hasDirectRoute(LightningCommonClient<?> origin, LightningCommonClient<?> destination);

    CommonQueryRouteResponse waitForRouteOrThrow(RouteVerification routeVerification);
}
