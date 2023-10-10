package org.tbk.spring.testcontainer.eps.config;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.eps",
        ignoreUnknownFields = false
)
public class ElectrumPersonalServerContainerProperties extends AbstractContainerProperties implements Validator {

    private static final Map<String, String> defaultEnvironment = ImmutableMap.<String, String>builder()
            .put("EPS_CONFIG", "")
            .build();
    /**
     * Specify the user to use on for RPC connections to bitcoind.
     */
    private String rpcuser;

    /**
     * Specify the password to use on for RPC connections to bitcoind.
     */
    private String rpcpass;

    /**
     * Specify the host to use on for RPC connections to bitcoind.
     */
    private String rpchost;

    /**
     * Specify the port to use on for RPC connections to bitcoind.
     */
    private Integer rpcport;

    public ElectrumPersonalServerContainerProperties() {
        super(Collections.emptyList(), defaultEnvironment);
    }

    @Override
    public Optional<String> getCommandValueByKey(String key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ElectrumPersonalServerContainerProperties.class;
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
        ElectrumPersonalServerContainerProperties properties = (ElectrumPersonalServerContainerProperties) target;

        String rpcuserValue = properties.getRpcuser();
        if (rpcuserValue == null) {
            String errorMessage = "'rpcuser' must not be null";
            errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
        } else if (rpcuserValue.isBlank()) {
            String errorMessage = "'rpcuser' must not be empty";
            errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
        } else if (containsWhitespaces(rpcuserValue)) {
            String errorMessage = String.format("'rpcuser' must not contain whitespaces - unsupported value: '%s'", rpcuserValue);
            errors.rejectValue("rpcuser", "rpcuser.unsupported", errorMessage);
        }

        String rpcpasswordValue = properties.getRpcpass();
        if (rpcpasswordValue == null) {
            String errorMessage = "'rpcpass' must not be null";
            errors.rejectValue("rpcpass", "rpcpass.invalid", errorMessage);
        } else if (rpcpasswordValue.isBlank()) {
            String errorMessage = "'rpcpass' must not be empty";
            errors.rejectValue("rpcpass", "rpcpass.invalid", errorMessage);
        } else if (containsWhitespaces(rpcpasswordValue)) {
            String errorMessage = "'rpcpass' must not contain whitespaces - unsupported value";
            errors.rejectValue("rpcpass", "rpcpass.unsupported", errorMessage);
        }

        String rpchostValue = properties.getRpchost();
        if (rpchostValue == null) {
            String errorMessage = "'rpchost' must not be null";
            errors.rejectValue("rpchost", "rpchost.invalid", errorMessage);
        } else if (rpchostValue.isBlank()) {
            String errorMessage = "'rpchost' must not be empty";
            errors.rejectValue("rpchost", "rpchost.invalid", errorMessage);
        } else if (containsWhitespaces(rpchostValue)) {
            String errorMessage = "'rpchost' must not contain whitespaces - unsupported value";
            errors.rejectValue("rpchost", "rpchost.unsupported", errorMessage);
        }

        Map<String, String> environment = properties.getEnvironment();

        String epsConfigEnvValue = environment.get("EPS_CONFIG");
        if (epsConfigEnvValue == null) {
            String errorMessage = "'EPS_CONFIG' must not be null";
            errors.rejectValue("environment", "environment.invalid", errorMessage);
        } else if (epsConfigEnvValue.isBlank()) {
            String errorMessage = "'EPS_CONFIG' must not be empty";
            errors.rejectValue("environment", "environment.invalid", errorMessage);
        }
    }

    private static boolean containsWhitespaces(String value) {
        return CharMatcher.whitespace().matchesAnyOf(value);
    }
}

