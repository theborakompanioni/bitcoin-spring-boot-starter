package org.tbk.spring.testcontainer.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;

// TODO: once an upgrade to spring boot 2.4.0 was made, this class should extend the "Validator" interface
//   in this version of spring boot the "validator" related classes are in an own module and can be integrated better
public abstract class AbstractContainerProperties implements ContainerProperties {

    private final List<String> reservedCommands;

    private final Map<String, String> defaultEnvironment;

    @Nullable
    private final DockerImageName defaultImage;

    private boolean enabled;

    private String image;

    private List<String> commands;

    private List<Integer> exposedPorts;

    private Map<String, String> environment;

    protected AbstractContainerProperties() {
        this(null, Collections.emptyList());
    }

    protected AbstractContainerProperties(@Nullable DockerImageName defaultImage) {
        this(defaultImage, Collections.emptyList());
    }

    /**
     * @apiNote Scheduled to be removed in v0.13.0
     * @deprecated use {@link #AbstractContainerProperties(DockerImageName, List<String>)}
     */
    @Deprecated
    protected AbstractContainerProperties(List<String> reservedCommands) {
        this(null, reservedCommands, Collections.emptyMap());
    }

    /**
     * @apiNote Scheduled to be removed in v0.13.0
     * @deprecated use {@link #AbstractContainerProperties(DockerImageName, List<String>, Map<String, String>)}
     */
    @Deprecated
    protected AbstractContainerProperties(List<String> reservedCommands, Map<String, String> defaultEnvironment) {
        this(null, reservedCommands, defaultEnvironment);
    }

    protected AbstractContainerProperties(@Nullable DockerImageName defaultImage, List<String> reservedCommands) {
        this(defaultImage, reservedCommands, Collections.emptyMap());
    }

    protected AbstractContainerProperties(@Nullable DockerImageName defaultImage, List<String> reservedCommands, Map<String, String> defaultEnvironment) {
        this.defaultImage = defaultImage;
        this.reservedCommands = ImmutableList.copyOf(reservedCommands);
        this.defaultEnvironment = ImmutableMap.copyOf(defaultEnvironment);
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final Optional<DockerImageName> getImage() {
        return Optional.ofNullable(image)
                .map(DockerImageName::parse)
                .or(this::getDefaultImage);
    }

    @Override
    public final Optional<DockerImageName> getDefaultImage() {
        return Optional.ofNullable(defaultImage);
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

    public void setImage(String image) {
        this.image = image;
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

