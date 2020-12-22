package org.tbk.spring.testcontainer.core;

import com.google.common.annotations.Beta;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

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

    /**
     * Whether the autconfiguration should be enabled
     */
    boolean isEnabled();

    default List<String> getCommands() {
        return Collections.emptyList();
    }

    default List<Integer> getExposedPorts() {
        return Collections.emptyList();
    }

    Optional<String> getCommandValueByKey(String key);

    private static boolean containsWhitespaces(String value) {
        return CharMatcher.WHITESPACE.matchesAnyOf(value);
    }
}

