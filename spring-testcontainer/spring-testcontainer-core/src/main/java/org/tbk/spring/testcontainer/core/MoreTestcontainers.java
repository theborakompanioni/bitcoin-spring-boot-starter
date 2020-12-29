package org.tbk.spring.testcontainer.core;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.base.CharMatcher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.ContainerState;

import java.util.Collection;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class MoreTestcontainers {

    private static final String testcontainersInternalHost = "host.testcontainers.internal";

    private static final String DEFAULT_CONTAINER_NAME_PREFIX = "tbk-testcontainer-";

    private static final TbkContainerCmdModifiers cmdModifiers = new TbkContainerCmdModifiers(DEFAULT_CONTAINER_NAME_PREFIX);

    private MoreTestcontainers() {
        throw new UnsupportedOperationException();
    }

    public static String testcontainersInternalHost() {
        return testcontainersInternalHost;
    }

    public static String buildInternalContainerUrl(ContainerState containerState, String protocol, int port) {
        return String.format("%s://%s", protocol, buildInternalContainerUrlWithoutProtocol(containerState, port));
    }

    public static String buildInternalHostUrl(String protocol, String user, String password, int port) {
        return String.format("%s://%s:%s@%s", protocol, user, password, buildHostUrlWithoutProtocol(port));
    }

    public static String buildInternalContainerUrl(ContainerState containerState, String protocol, String user, String password, int port) {
        return String.format("%s://%s:%s@%s", protocol, user, password, buildInternalContainerUrlWithoutProtocol(containerState, port));
    }

    public static String buildInternalContainerUrlWithoutProtocol(ContainerState containerState, int port) {
        return buildHostUrlWithoutProtocol(containerState.getMappedPort(port));
    }

    public static String buildHostUrlWithoutProtocol(int port) {
        return String.format("%s:%d", testcontainersInternalHost(), port);
    }

    public static void exposeAllPortsToOtherContainers(ContainerState containerState) {
        // expose all mapped ports of the host so other containers can communication with the given container
        exposePortsToOtherContainers(containerState, containerState.getExposedPorts());
    }

    public static void exposePortsToOtherContainers(ContainerState containerState, Collection<Integer> ports) {
        containerState.getExposedPorts().stream()
                .filter(port -> ports.stream().anyMatch(it -> it.equals(port)))
                .map(containerState::getMappedPort)
                .forEach(Testcontainers::exposeHostPorts);
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

