package org.tbk.mqtt.moquette.config;

import io.moquette.broker.config.MemoryConfig;

/**
 * Callback interface that can be implemented by beans wishing to customize
 * {@link MemoryConfig} before it is used.
 */
@FunctionalInterface
public interface MemoryConfigCustomizer {

    /**
     * Customize the memory config.
     *
     * @param config the {@link MemoryConfig} to customize
     */
    void customize(MemoryConfig config);

}