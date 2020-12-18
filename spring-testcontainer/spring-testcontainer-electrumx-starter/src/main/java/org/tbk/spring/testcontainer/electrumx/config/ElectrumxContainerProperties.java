package org.tbk.spring.testcontainer.electrumx.config;

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
        prefix = "org.tbk.spring.testcontainer.electrumx",
        ignoreUnknownFields = false
)
public class ElectrumxContainerProperties implements Validator {

    /**
     * a list of reserved commands
     * <p>
     * some commands must have predefined values during the bootstrapping of the container.
     * e.g.
     * - `--bitcoin.active`
     * - `--bitcoin.regtest`
     * - `--bitcoin.node=bitcoind`
     * <p>
     * these commands should be disallowed to be specified by a user.
     * <p>
     * this behaviour is subject to change. i do not like it.
     */
    @Beta
    private static final Set<String> reservedCommands = ImmutableSet.<String>builder()
            .add("restlisten")
            .add("rpclisten")
            .add("noseedbackup")
            .add("bitcoin.active")
            .add("bitcoin.regtest")
            .add("bitcoin.node")
            .add("bitcoin.rpchost")
            .add("bitcoin.zmqpubrawblock")
            .add("bitcoin.zmqpubrawtx")
            .build();

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

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

    private List<String> commands;

    private List<Integer> exposedPorts;

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
        return clazz == ElectrumxContainerProperties.class;
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
        ElectrumxContainerProperties properties = (ElectrumxContainerProperties) target;

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
            errors.rejectValue("rpcpass", "rpcuser.invalid", errorMessage);
        } else if (rpcpasswordValue.isBlank()) {
            String errorMessage = "'rpcpass' must not be empty";
            errors.rejectValue("rpcpass", "rpcpassword.invalid", errorMessage);
        } else if (containsWhitespaces(rpcpasswordValue)) {
            String errorMessage = "'rpcpass' must not contain whitespaces - unsupported value";
            errors.rejectValue("rpcpass", "rpcpassword.unsupported", errorMessage);
        }

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

