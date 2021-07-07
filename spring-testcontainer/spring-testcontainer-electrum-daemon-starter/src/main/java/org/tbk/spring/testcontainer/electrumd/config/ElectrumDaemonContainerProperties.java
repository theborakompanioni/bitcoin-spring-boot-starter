package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.electrum-daemon",
        ignoreUnknownFields = false
)
public class ElectrumDaemonContainerProperties extends AbstractContainerProperties implements Validator {
    static final String ELECTRUM_USER_ENV_NAME = "ELECTRUM_USER";
    static final String ELECTRUM_HOME_ENV_NAME = "ELECTRUM_HOME";
    static final String ELECTRUM_PASSWORD_ENV_NAME = "ELECTRUM_PASSWORD";
    static final String ELECTRUM_NETWORK_ENV_NAME = "ELECTRUM_NETWORK";

    private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
            .put(ELECTRUM_USER_ENV_NAME, "electrum")
            .put(ELECTRUM_HOME_ENV_NAME, "/home/electrum")
            .put(ELECTRUM_NETWORK_ENV_NAME, "regtest")
            .build();

    /**
     * (Optional) Specify the wallet that electrum should open on startup.
     */
    private String defaultWallet;

    public ElectrumDaemonContainerProperties() {
        super(Collections.emptyList(), defaultEnvironment);
    }

    public Optional<String> getDefaultWallet() {
        return Optional.ofNullable(defaultWallet);
    }

    public String getNetwork() {
        return requireNonNull(getEnvironmentWithDefaults().get(ELECTRUM_NETWORK_ENV_NAME));
    }

    public String getElectrumHomeDir() {
        return requireNonNull(getEnvironmentWithDefaults().get(ELECTRUM_HOME_ENV_NAME));
    }

    @Override
    public Optional<String> getCommandValueByKey(String key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ElectrumDaemonContainerProperties.class;
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
        ElectrumDaemonContainerProperties properties = (ElectrumDaemonContainerProperties) target;

        if (!properties.isEnabled()) {
            return;
        }

        errors.pushNestedPath("environment");
        Map<String, String> environment = properties.getEnvironmentWithDefaults();
        Set<String> envKeys = Set.of(ELECTRUM_HOME_ENV_NAME, ELECTRUM_USER_ENV_NAME, ELECTRUM_PASSWORD_ENV_NAME, ELECTRUM_NETWORK_ENV_NAME);
        envKeys.forEach(field -> {
            String value = environment.get(field);
            if (value == null || value.isBlank()) {
                String errorMessage = String.format("'%s' must not be empty - invalid value: %s", field, value);
                errors.rejectValue(field, field + ".invalid", errorMessage);
            }
        });
        errors.popNestedPath();
    }
}

