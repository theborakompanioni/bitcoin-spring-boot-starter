package org.tbk.spring.testcontainer.core;

import com.google.common.annotations.Beta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContainerProperties {

    /**
     * a list of reserved commands
     * <p>
     * some commands must have predefined values during the bootstrapping of the container.
     * <p>
     * these commands should be disallowed to be specified by a user.
     * <p>
     * this behaviour is subject to change. i do not like it.
     */
    @Beta
    default List<String> getReservedCommands() {
        return Collections.emptyList();
    }

    @Beta
    default Map<String, String> getDefaultEnvironment() {
        return Collections.emptyMap();
    }

    /**
     * Whether the autoconfiguration should be enabled
     */
    boolean isEnabled();

    default List<String> getCommands() {
        return Collections.emptyList();
    }

    default List<Integer> getExposedPorts() {
        return Collections.emptyList();
    }

    default Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

    Optional<String> getCommandValueByKey(String key);
}

