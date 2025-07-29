package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.electrum-daemon",
        ignoreUnknownFields = false
)
public class ElectrumDaemonContainerProperties extends AbstractContainerProperties implements Validator {
    static final String ELECTRUM_RPCUSER_ENV_NAME = "ELECTRUM_RPCUSER";
    static final String ELECTRUM_RPCPASSWORD_ENV_NAME = "ELECTRUM_RPCPASSWORD";
    static final String ELECTRUM_NETWORK_ENV_NAME = "ELECTRUM_NETWORK";

    private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
            .put(ELECTRUM_RPCUSER_ENV_NAME, "electrum")
            .put(ELECTRUM_NETWORK_ENV_NAME, "regtest")
            .build();

    /**
     * (Optional) Specify the wallet that electrum should open on startup.
     */
    private String defaultWallet;

    /**
     * (Optional) Specify multiple wallet that should be copied to the container.
     */
    private List<String> wallets;

    public ElectrumDaemonContainerProperties() {
        super(null, Collections.emptyList(), defaultEnvironment);
    }

    public Optional<String> getDefaultWallet() {
        return Optional.ofNullable(defaultWallet);
    }

    public List<String> getWallets() {
        return Collections.unmodifiableList(requireNonNullElseGet(wallets, Collections::emptyList));
    }

    public String getNetwork() {
        return requireNonNull(getEnvironmentWithDefaults().get(ELECTRUM_NETWORK_ENV_NAME));
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
     *
     * <p>Keep in mind that Testcontainers splits commands on whitespaces.
     * This means, every property that is part of a command, must not contain whitespaces.
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
        Set<String> envKeys = Set.of(ELECTRUM_RPCUSER_ENV_NAME, ELECTRUM_RPCPASSWORD_ENV_NAME, ELECTRUM_NETWORK_ENV_NAME);
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

