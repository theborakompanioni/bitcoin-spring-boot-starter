package org.tbk.lightning.client.common.core;

import org.tbk.lightning.client.common.core.proto.ConnectRequest;
import org.tbk.lightning.client.common.core.proto.ConnectResponse;
import org.tbk.lightning.client.common.core.proto.GetinfoRequest;
import org.tbk.lightning.client.common.core.proto.GetinfoResponse;
import reactor.core.publisher.Mono;

public interface LnCommonClient<T> {

    Mono<GetinfoResponse> info(GetinfoRequest request);

    Mono<ConnectResponse> connect(ConnectRequest request);

    // TODO: consider removing access to the underlying client after all common functions are implemented
    T baseClient();

}
