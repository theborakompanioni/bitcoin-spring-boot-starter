package org.tbk.spring.testcontainer.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use this class if you only want certain exposed ports to be checked, an not all of them.
 *
 * <p>Default HostPortWaitStrategy would ask the waitStrategyTarget
 * for all "liveness check ports" and use all exposed ports of the container.
 * This is sometimes unwanted as e.g. in case of zeromq, ports must be exposed,
 * but will fail during dockers liveness check.
 */
@Value
@Builder
@EqualsAndHashCode(callSuper = false)
public class CustomHostPortWaitStrategy extends HostPortWaitStrategy {
    @Singular("addPort")
    List<Integer> ports;

    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        return ports.stream()
                .map(val -> waitStrategyTarget.getMappedPort(val))
                .collect(Collectors.toSet());
    }
}
