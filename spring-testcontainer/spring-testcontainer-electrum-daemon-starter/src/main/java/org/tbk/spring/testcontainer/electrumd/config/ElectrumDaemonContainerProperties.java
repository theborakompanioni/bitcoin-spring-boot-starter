package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.electrum-daemon",
        ignoreUnknownFields = false
)
public class ElectrumDaemonContainerProperties implements Validator {
    private static final int DEFAULT_RPC_PORT = 7000;

    /**
     * a list of reserved commands
     * <p>
     * some commands must have predefined values during the bootstrapping of the container.
     * <p>
     * these commands should be disallowed to be specified by a user.
     * <p>
     * this behaviour is subject to change. i do not like it.
     */
    @Beta
    private static final Set<String> reservedCommands = ImmutableSet.<String>builder()
            .build();

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    /**
     * Specify the port to open for incoming RPC connections.
     */
    private Integer rpcport;

    private List<String> commands;

    private List<Integer> exposedPorts;

    public int getRpcPort() {
        return Optional.ofNullable(rpcport).orElse(DEFAULT_RPC_PORT);
    }

    public List<String> getCommands() {
        return commands == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(commands);
    }

    public List<Integer> getExposedPorts() {
        return exposedPorts == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(exposedPorts);
    }

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
                    return it.split(commandWithPrefix + "=")[1];
                })
                .findFirst();
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ElectrumDaemonContainerProperties.class;
    }

    /**
     * Validate the container properties.
     * <p>
     * Keep in mind that Testcontainers splits commands on whitespaces.
     * This means, every property that is part of a command, must not contain
     * whitespaces.
     *
     * @param target
     * @param errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        ElectrumDaemonContainerProperties properties = (ElectrumDaemonContainerProperties) target;

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

