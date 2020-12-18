package org.tbk.spring.testcontainer.core;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.base.CharMatcher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class MoreTestcontainers {
    private static final String DEFAULT_CONTAINER_NAME_PREFIX = "tbk-testcontainer-";

    private static final TbkContainerCmdModifiers cmdModifiers = new TbkContainerCmdModifiers(DEFAULT_CONTAINER_NAME_PREFIX);

    private MoreTestcontainers() {
        throw new UnsupportedOperationException();
    }

    public static TbkContainerCmdModifiers cmdModifiers() {
        return cmdModifiers;
    }

    @RequiredArgsConstructor
    public final static class TbkContainerCmdModifiers {
        private static final String digits1234567890 = "123456789";
        private static final String abcdefghijklmnopqrstuvwxyz = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private static final CharMatcher validNameCharMatcher = CharMatcher.anyOf(digits1234567890)
                .or(CharMatcher.anyOf(abcdefghijklmnopqrstuvwxyz.toUpperCase()))
                .or(CharMatcher.anyOf(abcdefghijklmnopqrstuvwxyz.toLowerCase()))
                .or(CharMatcher.anyOf("_-"))
                .precomputed();

        @NonNull
        private final String containerNamePrefix;

        public Consumer<CreateContainerCmd> withName(String name) {
            requireNonNull(name);

            String cleanName = validNameCharMatcher.retainFrom(name);
            if (cleanName.isBlank()) {
                throw new IllegalArgumentException("'name' is empty after removing illegal chars - given: " + name);
            }

            String nameWithPrefix = containerNamePrefix + cleanName;

            return createContainerCmd -> createContainerCmd.withName(nameWithPrefix);
        }
    }
}

