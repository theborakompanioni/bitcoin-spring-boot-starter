package org.tbk.electrum.config;

/**
 * Callback interface that can be implemented by beans wishing to customize
 * Electrum Daemon JSON RPC config {@link ElectrumDeamonJsonrpcConfigBuilder} before it is used.
 */
@FunctionalInterface
public interface ElectrumDeamonJsonrpcConfigBuilderCustomizer {

    /**
     * Customize the json rpc config.
     *
     * @param config the {@link ElectrumDeamonJsonrpcConfigBuilder} to customize
     */
    void customize(ElectrumDeamonJsonrpcConfigBuilder config);

}