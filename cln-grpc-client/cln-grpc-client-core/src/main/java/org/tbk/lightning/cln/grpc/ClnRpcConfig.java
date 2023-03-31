package org.tbk.lightning.cln.grpc;

public interface ClnRpcConfig {

    /**
     * IP address or hostname where cln grpc api is reachable.
     * e.g. localhost, 192.168.0.1, etc.
     *
     * @return IP address or hostname where cln grpc api is reachable.
     */
    String getHost();

    /**
     * Port where cln grpc api is listening.
     *
     * @return Port where cln grpc api is listening.
     */
    Integer getPort();
}
