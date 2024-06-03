package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Properties for bitcoind testcontainer.
 *
 * <p>Mainnet
 * JSON-RPC/REST: 8332
 * P2P: 8333
 *
 * <p>Testnet
 * Testnet JSON-RPC: 18332
 * P2P: 18333
 *
 * <p>Regtest
 * JSON-RPC/REST: 18443 (since 0.16+, otherwise 18332)
 * P2P: 18444
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.bitcoind",
        ignoreUnknownFields = false
)
public class BitcoindContainerProperties extends AbstractContainerProperties implements Validator {
    private static final String DEFAULT_DOCKER_IMAGE_NAME = "polarlightning/bitcoind:26.0";
    private static final DockerImageName defaultDockerImageName = DockerImageName.parse(DEFAULT_DOCKER_IMAGE_NAME);

    private static final int MAINNET_DEFAULT_RPC_PORT = 8332;
    private static final int MAINNET_DEFAULT_P2P_PORT = 8333;

    private static final int TESTNET_DEFAULT_RPC_PORT = 18332;
    private static final int TESTNET_DEFAULT_P2P_PORT = 18333;

    private static final int REGTEST_DEFAULT_RPC_PORT = 18443;
    private static final int REGTEST_DEFAULT_P2P_PORT = 18444;

    private static final Network DEFAULT_NETWORK = Network.regtest;

    @Beta
    private static final List<String> reservedCommands = ImmutableList.<String>builder()
            .add("rpcuser")
            .add("rpcpassword")
            .add("rpcport")
            .add("port")
            .add("chain")
            .add("testnet")
            .add("regtest")
            .add("signet")
            .build();

    /**
     * See <a href="https://github.com/bitcoin/bitcoin/blob/v27.0/src/chainparamsbase.cpp">chainparamsbase.cpp</a>.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Network {
        mainnet("main"),
        testnet("test"),
        regtest("regtest");

        @NonNull
        private final String chain;
    }

    public BitcoindContainerProperties() {
        super(defaultDockerImageName, reservedCommands);
    }

    private Network network;

    /**
     * RPC port
     */
    private Integer rpcport;

    /**
     * P2P Port
     */
    private Integer port;

    /**
     * RPC username.
     */
    private String rpcuser;

    /**
     * RPC password.
     */
    private String rpcpassword;

    public Network getNetwork() {
        return this.network != null ? this.network : DEFAULT_NETWORK;
    }

    public Optional<String> getRpcuser() {
        return Optional.ofNullable(rpcuser);
    }

    public Optional<String> getRpcpassword() {
        return Optional.ofNullable(rpcpassword);
    }

    public int getRpcport() {
        return rpcport != null ? rpcport : switch (getNetwork()) {
            case mainnet -> MAINNET_DEFAULT_RPC_PORT;
            case testnet -> TESTNET_DEFAULT_RPC_PORT;
            case regtest -> REGTEST_DEFAULT_RPC_PORT;
        };
    }

    public int getPort() {
        return port != null ? port : switch (getNetwork()) {
            case mainnet -> MAINNET_DEFAULT_P2P_PORT;
            case testnet -> TESTNET_DEFAULT_P2P_PORT;
            case regtest -> REGTEST_DEFAULT_P2P_PORT;
        };
    }

    public Optional<String> getCommandValueByKey(String key) {
        String commandWithPrefix = "-" + key;
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
        return clazz == BitcoindContainerProperties.class;
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
        BitcoindContainerProperties properties = (BitcoindContainerProperties) target;

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

        int rpcportValue = properties.getRpcport();
        if (!isValidPort(rpcportValue)) {
            String errorMessage = "'rpcport' must be in the range 0-65535";
            errors.rejectValue("rpcport", "rpcport.invalid", errorMessage);
        }

        int p2pport = properties.getPort();
        if (!isValidPort(p2pport)) {
            String errorMessage = "'port' must be in the range 0-65535";
            errors.rejectValue("port", "port.invalid", errorMessage);
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
        return CharMatcher.whitespace().matchesAnyOf(value);
    }

    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}

