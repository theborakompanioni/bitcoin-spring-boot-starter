package org.tbk.spring.testcontainer.core;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

// TODO: once an upgrade to spring boot 2.4.0 was made, this class should extend the "Validator" interface
//   in this version of spring boot the "validator" related classes are in an own module and can be integrated better
public abstract class AbstractContainerProperties implements ContainerProperties {

    private boolean enabled;

    private List<String> reservedCommands;

    private List<String> commands;

    private List<Integer> exposedPorts;

    protected AbstractContainerProperties() {
        this(Collections.emptyList());
    }

    protected AbstractContainerProperties(List<String> reservedCommands) {
        this.reservedCommands = ImmutableList.copyOf(reservedCommands);
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
    public final List<String> getCommands() {
        return commands == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(commands);
    }

    @Override
    public final List<Integer> getExposedPorts() {
        return exposedPorts == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(exposedPorts);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(commands);
    }

    public void setExposedPorts(List<Integer> exposedPorts) {
        this.exposedPorts = exposedPorts == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(exposedPorts);
    }
}

