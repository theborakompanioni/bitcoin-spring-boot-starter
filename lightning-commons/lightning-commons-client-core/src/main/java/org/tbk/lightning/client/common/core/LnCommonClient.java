package org.tbk.lightning.client.common.core;

import org.tbk.lightning.client.common.core.proto.GetinfoRequest;
import org.tbk.lightning.client.common.core.proto.GetinfoResponse;
import reactor.core.publisher.Mono;

public interface LnCommonClient {

    Mono<GetinfoResponse> getInfo(GetinfoRequest request);

}
