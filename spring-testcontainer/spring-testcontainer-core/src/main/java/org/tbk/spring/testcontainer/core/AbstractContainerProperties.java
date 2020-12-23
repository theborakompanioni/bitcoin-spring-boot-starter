package org.tbk.spring.testcontainer.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;

// TODO: once an upgrade to spring boot 2.4.0 was made, this class should extend the "Validator" interface
//   in this version of spring boot the "validator" related classes are in an own module and can be integrated better
public abstract class AbstractContainerProperties implements ContainerProperties {

    private boolean enabled;

    private final List<String> reservedCommands;

    private final Map<String, String> defaultEnvironment;

    private List<String> commands;

    private List<Integer> exposedPorts;

    private Map<String, String> environment;

    protected AbstractContainerProperties() {
        this(Collections.emptyList());
    }

    protected AbstractContainerProperties(List<String> reservedCommands) {
        this(reservedCommands, Collections.emptyMap());
    }

    protected AbstractContainerProperties(List<String> reservedCommands, Map<String, String> defaultEnvironment) {
        this.reservedCommands = ImmutableList.copyOf(reservedCommands);
        this.defaultEnvironment = ImmutableMap.copyOf(defaultEnvironment);
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final List<String> getReservedCommands() {
        return this.reservedCommands;
    }

    @Override
    public final Map<String, String> getDefaultEnvironment() {
        return this.defaultEnvironment;
    }

    @Override
    public final List<String> getCommands() {
        return ImmutableList.copyOf(firstNonNull(this.commands, Collections.emptyList()));
    }

    @Override
    public final List<Integer> getExposedPorts() {
        return ImmutableList.copyOf(firstNonNull(this.exposedPorts, Collections.emptyList()));
    }

    @Override
    public final Map<String, String> getEnvironment() {
        return ImmutableMap.copyOf(firstNonNull(this.environment, Collections.emptyMap()));
    }

    public Map<String, String> getEnvironmentWithDefaults() {
        Map<String, String> userGivenEnvVars = ImmutableMap.copyOf(firstNonNull(this.environment, Collections.emptyMap()));

        Map<String, String> defaultEnvVars = this.getDefaultEnvironment().entrySet().stream()
                .filter(it -> !userGivenEnvVars.containsKey(it.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ImmutableMap.<String, String>builder()
                .putAll(userGivenEnvVars)
                .putAll(defaultEnvVars)
                .build();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCommands(List<String> commands) {
        this.commands = ImmutableList.copyOf(firstNonNull(commands, Collections.emptyList()));
    }

    public void setExposedPorts(List<Integer> exposedPorts) {
        this.exposedPorts = ImmutableList.copyOf(firstNonNull(exposedPorts, Collections.emptyList()));
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = ImmutableMap.copyOf(firstNonNull(environment, Collections.emptyMap()));
    }
}

