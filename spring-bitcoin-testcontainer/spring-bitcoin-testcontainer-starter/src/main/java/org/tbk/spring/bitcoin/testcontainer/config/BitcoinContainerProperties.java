package org.tbk.spring.bitcoin.testcontainer.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.bitcoin.testcontainer",
        ignoreUnknownFields = false
)
public class BitcoinContainerProperties implements Validator {

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    /**
     * RPC username
     */
    private String rpcuser;

    /**
     * RPC password
     */
    private String rpcpassword;

    private List<String> commands;

    public Optional<String> getRpcuser() {
        return Optional.ofNullable(rpcuser);
    }

    public Optional<String> getRpcpassword() {
        return Optional.ofNullable(rpcpassword);
    }

    public List<String> getCommands() {
        return commands == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(commands);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BitcoinContainerProperties.class;
    }

    /**
     * Validate the container properties.
     *
     * Keep in mind that Testcontainers splits commands on whitespaces.
     * This means, every property that is part of a command, must not contain
     * whitespaces. Error early when user gave an "unsupported" value
     * (the value might be "valid" but it is just not supported. e.g. rpcpassword with a whitespace :/ )
     *
     * @param target
     * @param errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        BitcoinContainerProperties properties = (BitcoinContainerProperties) target;

        String rpcuserValue = properties.getRpcuser().orElse(null);
        if (rpcuserValue != null) {
            if (rpcuserValue.isBlank()) {
                String errorMessage = "'rpcuser' must not be empty";
                errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
            } else if (containsWhitespaces(rpcuserValue)) {
                String errorMessage = String.format("'rpcuser' must not contain whitespaces - unsupported value: '%s'", rpcuserValue);
                errors.rejectValue("rpcuser", "rpcuser.unsupported", errorMessage);
            }
        }

        String rpcpasswordValue = properties.getRpcpassword().orElse(null);
        if (rpcpasswordValue != null) {
            if (rpcpasswordValue.isBlank()) {
                String errorMessage = "'rpcpassword' must not be empty";
                errors.rejectValue("rpcpassword", "rpcpassword.invalid", errorMessage);
            } else if (containsWhitespaces(rpcpasswordValue)) {
                String errorMessage = "'rpcpassword' must not contain whitespaces - unsupported value";
                errors.rejectValue("rpcpassword", "rpcpassword.unsupported", errorMessage);
            }
        }

        for (String command : properties.getCommands()) {
            if (Strings.isNullOrEmpty(command)) {
                String errorMessage = "'commands' entry must not be empty";
                errors.rejectValue("commands", "commands.invalid", errorMessage);
            } else {
                boolean startsWithHyphen = command.startsWith("-");
                if (!startsWithHyphen) {
                    String errorMessage = String.format(
                            "'commands' entry must start with '-': invalid value: %s",
                            command
                    );
                    errors.rejectValue("commands", "commands.invalid", errorMessage);
                }

                if (containsWhitespaces(command)) {
                    String errorMessage = String.format(
                            "'commands' entry must not contain whitespaces: unsupported value: %s",
                            command
                    );
                    errors.rejectValue("commands", "commands.unsupported", errorMessage);
                }
            }
        }
    }

    private static boolean containsWhitespaces(String value) {
        return CharMatcher.WHITESPACE.matchesAnyOf(value);
    }
}

