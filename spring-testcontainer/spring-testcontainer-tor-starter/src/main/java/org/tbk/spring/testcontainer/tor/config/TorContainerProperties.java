package org.tbk.spring.testcontainer.tor.config;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.tor",
        ignoreUnknownFields = false
)
public class TorContainerProperties extends AbstractContainerProperties implements Validator {

    private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
            .put("TOR_EXTRA_ARGS", "")
            .build();

    private Map<String, HiddenServiceDefinition> hiddenServices;

    public TorContainerProperties() {
        super(Collections.emptyList(), defaultEnvironment);
    }

    public List<Integer> getHiddenServiceHostPorts() {
        return getHiddenServices().values().stream()
                .map(HiddenServiceDefinition::getHostPort)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getCommandValueByKey(String key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == TorContainerProperties.class;
    }

    /**
     * Validate the container properties.
     * <p>
     * Keep in mind that Testcontainers splits commands on whitespaces.
     * This means, every property that is part of a command, must not contain whitespaces.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void validate(Object target, Errors errors) {
        TorContainerProperties properties = (TorContainerProperties) target;

    }

    @Data
    public static class HiddenServiceDefinition {
        private String directoryName;
        private int virtualPort;
        private int hostPort;
    }
}

