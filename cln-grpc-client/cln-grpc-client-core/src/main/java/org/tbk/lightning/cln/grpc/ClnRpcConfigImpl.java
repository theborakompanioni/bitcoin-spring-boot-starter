package org.tbk.lightning.cln.grpc;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ClnRpcConfigImpl implements ClnRpcConfig {

    /**
     * IP address or hostname including http:// or https:// where cln grpc api is reachable.
     * e.g. https://localhost:10001
     *
     * @return IP address or hostname including http:// or https:// where cln grpc api is reachable.
     */
    @NonNull
    String host;

    /**
     * Port where cln grpc api is listening.
     *
     * @return Port where cln grpc api is listening.
     */
    @NonNull
    Integer port;

}
