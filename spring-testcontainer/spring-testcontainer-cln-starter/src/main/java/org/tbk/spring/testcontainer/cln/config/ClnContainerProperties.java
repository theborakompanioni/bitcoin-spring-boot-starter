package org.tbk.spring.testcontainer.cln.config;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.cln",
        ignoreUnknownFields = false
)
public class ClnContainerProperties extends AbstractContainerProperties implements Validator {
    private static final int DEFAULT_PORT = 9735;
    private static final int DEFAULT_RPC_PORT = 9835;

    static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofMinutes(1);

    @Beta
    private static final List<String> reservedCommands = ImmutableList.<String>builder()
            .add("bitcoin-rpcconnect")
            .add("bitcoin-rpcport")
            .build();

    public ClnContainerProperties() {
        super(reservedCommands);
    }

    /**
     * Specify the port to listen on for RPC connections.
     */
    private Integer rpcport;

    private Integer port;

    public int getRpcport() {
        return rpcport != null ? rpcport : DEFAULT_RPC_PORT;
    }

    public int getPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    public Optional<String> getRpcuser() {
        return getCommandValueByKey("bitcoin-rpcuser");
    }

    public Optional<String> getRpcpass() {
        return getCommandValueByKey("bitcoin-rpcpassword");
    }

    @Override
    public Optional<String> getCommandValueByKey(String key) {
        String commandWithPrefix = "--" + key;
        return this.getCommands().stream()
                .filter(it -> it.startsWith(commandWithPrefix))
                .map(it -> {
                    boolean withoutValue = commandWithPrefix.length() == it.length();
                    if (withoutValue) {
                        return "";
                    }

                    checkArgument('=' == it.charAt(commandWithPrefix.length()));
                    return it.split(commandWithPrefix + "=", 2)[1];
                })
                .findFirst();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ClnContainerProperties.class;
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
        ClnContainerProperties properties = (ClnContainerProperties) target;

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

        String rpcpasswordValue = properties.getRpcpass().orElse(null);
        if (rpcpasswordValue != null) {
            if (rpcpasswordValue.isBlank()) {
                String errorMessage = "'rpcpass' must not be empty";
                errors.rejectValue("rpcpass", "rpcpassword.invalid", errorMessage);
            } else if (containsWhitespaces(rpcpasswordValue)) {
                String errorMessage = "'rpcpass' must not contain whitespaces - unsupported value";
                errors.rejectValue("rpcpass", "rpcpassword.unsupported", errorMessage);
            }
        }

        // lnd enforced a 32 char limit on alias. let's do the same for cln.
        properties.getCommandValueByKey("alias").ifPresent(it -> {
            if (it.length() > 32) {
                String errorMessage = "'alias' must not be longer than 32 chars";
                errors.rejectValue("alias", "alias.length", errorMessage);
            }
        });

        reservedCommands.stream()
                .filter(val -> properties.getCommandValueByKey(val).isPresent())
                .forEach(reservedKey -> {
                    String errorMessage = String.format("'commands' entry must not contain key '%s'", reservedKey);
                    errors.rejectValue("commands", "commands.reserved", errorMessage);
                });

        for (String command : properties.getCommands()) {
            if (Strings.isNullOrEmpty(command)) {
                String errorMessage = "'commands' entry must not be empty";
                errors.rejectValue("commands", "commands.invalid", errorMessage);
            } else {
                boolean startsWithHyphen = command.startsWith("--");
                if (!startsWithHyphen) {
                    String errorMessage = String.format(
                            "'commands' entry must start with '--': invalid value: '%s'",
                            command
                    );
                    errors.rejectValue("commands", "commands.invalid", errorMessage);
                }

                if (containsWhitespaces(command)) {
                    String errorMessage = String.format(
                            "'commands' entry must not contain whitespaces: unsupported value: '%s'",
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

