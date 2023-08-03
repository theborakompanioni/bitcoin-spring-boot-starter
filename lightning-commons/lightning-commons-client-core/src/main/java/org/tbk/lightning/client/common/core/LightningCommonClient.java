package org.tbk.lightning.client.common.core;

import org.tbk.lightning.client.common.core.proto.CommonConnectRequest;
import org.tbk.lightning.client.common.core.proto.CommonConnectResponse;
import org.tbk.lightning.client.common.core.proto.CommonInfoRequest;
import org.tbk.lightning.client.common.core.proto.CommonInfoResponse;
import reactor.core.publisher.Mono;

public interface LightningCommonClient<T> {
    Mono<CommonInfoResponse> info(CommonInfoRequest request);

    Mono<CommonConnectResponse> connect(CommonConnectRequest request);

    // TODO: consider removing access to the underlying client after all common functions are implemented
    T baseClient();
}
