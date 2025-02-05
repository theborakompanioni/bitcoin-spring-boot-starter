package org.tbk.bitcoin.zeromq.config;

import org.tbk.bitcoin.zeromq.config.BitcoinZmqClientConfig.BitcoinZmqClientConfigBuilder;

/**
 * Callback interface that can be implemented by beans wishing to customize Bitcoin ZeroMq config
 * {@link BitcoinZmqClientConfigBuilder} before it is used.
 */
@FunctionalInterface
public interface BitcoinZmqClientConfigBuilderCustomizer {

    /**
     * Customize the json rpc config.
     *
     * @param config the {@link BitcoinZmqClientConfigBuilder} to customize
     */
    void customize(BitcoinZmqClientConfigBuilder config);

}
