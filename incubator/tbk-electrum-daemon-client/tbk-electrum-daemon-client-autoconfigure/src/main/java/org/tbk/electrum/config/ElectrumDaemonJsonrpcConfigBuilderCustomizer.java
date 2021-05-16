package org.tbk.electrum.config;

/**
 * Callback interface that can be implemented by beans wishing to customize
 * Electrum Daemon JSON RPC config {@link ElectrumDaemonJsonrpcConfigBuilder} before it is used.
 */
@FunctionalInterface
public interface ElectrumDaemonJsonrpcConfigBuilderCustomizer {

    /**
     * Customize the json rpc config.
     *
     * @param config the {@link ElectrumDaemonJsonrpcConfigBuilder} to customize
     */
    void customize(ElectrumDaemonJsonrpcConfigBuilder config);

}