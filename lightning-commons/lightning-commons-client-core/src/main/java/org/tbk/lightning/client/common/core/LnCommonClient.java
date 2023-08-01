package org.tbk.lightning.client.common.core;

import org.tbk.lightning.client.common.core.proto.GetinfoRequest;
import org.tbk.lightning.client.common.core.proto.GetinfoResponse;
import reactor.core.publisher.Mono;

public interface LnCommonClient<T> {

    Mono<GetinfoResponse> info(GetinfoRequest request);

    // TODO: consider removing access to the underlying client after all common functions are implemented
    T baseClient();

}
