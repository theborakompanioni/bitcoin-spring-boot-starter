package org.tbk.lightning.cln.grpc;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ClnRpcConfigImpl implements ClnRpcConfig {

    /**
     * IP address or hostname where cln grpc api is reachable.
     * e.g. localhost, 192.168.0.1, etc.
     *
     * @param host IP address or hostname where cln grpc api is reachable.
     * @return IP address or hostname where cln grpc api is reachable.
     */
    @NonNull
    String host;

    /**
     * Port where cln grpc api is listening.
     *
     * @param port Port where cln grpc api is listening.
     * @return Port where cln grpc api is listening.
     */
    @NonNull
    Integer port;

}
