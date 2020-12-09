package org.tbk.bitcoin.jsonrpc.config;

/**
 * Callback interface that can be implemented by beans wishing to customize Bitcoin JSON RPC config
 * {@link RpcConfigBuilder} before it is used.
 */
@FunctionalInterface
public interface RpcConfigBuilderCustomizer {

    /**
     * Customize the json rpc config.
     *
     * @param config the {@link RpcConfigBuilder} to customize
     */
    void customize(RpcConfigBuilder config);

}