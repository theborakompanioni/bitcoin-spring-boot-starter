package org.tbk.lightning.cln.grpc.config;

import io.grpc.ManagedChannelBuilder;

/**
 * Callback interface that can be implemented by beans wishing to customize CLN grpc channel
 * {@link io.grpc.ManagedChannelBuilder} before it is used.
 */
@FunctionalInterface
public interface ManagedChannelBuilderCustomizer {

    /**
     * Customize the channel builder.
     *
     * @param config the {@link io.grpc.ManagedChannelBuilder} to customize
     */
    void customize(ManagedChannelBuilder config);

}