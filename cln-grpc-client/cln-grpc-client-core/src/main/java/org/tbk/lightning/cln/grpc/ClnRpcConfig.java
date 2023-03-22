package org.tbk.lightning.cln.grpc;

public interface ClnRpcConfig {

    /**
     * IP address or hostname including http:// or https:// where cln grpc api is reachable.
     * e.g. https://localhost:10001
     *
     * @return IP address or hostname including http:// or https:// where cln grpc api is reachable.
     */
    String getHost();

    /**
     * Port where cln grpc api is listening.
     *
     * @return Port where cln grpc api is listening.
     */
    Integer getPort();
}
