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
import org.testcontainers.utility.DockerImageName;

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
    private static final String DEFAULT_DOCKER_IMAGE_NAME = "polarlightning/clightning:24.11.1";
    private static final DockerImageName defaultDockerImageName = DockerImageName.parse(DEFAULT_DOCKER_IMAGE_NAME);

    private static final String DEFAULT_NETWORK = "regtest";

    // If 'PORT' is not specified, the default port 9735 is used for mainnet
    // (testnet: 19735, signet: 39735, regtest: 19846)
    private static final int DEFAULT_MAINNET_PORT = 9735;
    private static final int DEFAULT_TESTNET_PORT = 19735;
    private static final int DEFAULT_SIGNET_PORT = 39735;
    private static final int DEFAULT_REGTEST_PORT = 19846;

    private static int getPortByNetwork(String network) {
        return switch (network) {
            case "bitcoin" -> DEFAULT_MAINNET_PORT;
            case "testnet" -> DEFAULT_TESTNET_PORT;
            case "signet" -> DEFAULT_SIGNET_PORT;
            case "regtest" -> DEFAULT_REGTEST_PORT;
            default -> throw new IllegalStateException("Unexpected value: " + network);
        };
    }


    static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofMinutes(1);

    @Beta
    private static final List<String> reservedCommands = ImmutableList.<String>builder()
            .add("bitcoin-rpcconnect")
            .add("bitcoin-rpcport")
            .build();

    public ClnContainerProperties() {
        super(defaultDockerImageName, reservedCommands);
    }

    private Integer port;

    /**
     * Specify the port to listen on for RPC connections.
     *
     * @deprecated scheduled for removal; use the grpc instead
     */
    @Deprecated
    private Integer rpcport;

    @Deprecated
    public int getRpcport() {
        return rpcport != null ? rpcport : 9835;
    }

    public int getPort() {
        return port != null ? port : getPortByNetwork(getNetwork());
    }

    public Optional<String> getBitcoinRpcUser() {
        return getCommandValueByKey("bitcoin-rpcuser");
    }

    public Optional<String> getBitcoinRpcPassword() {
        return getCommandValueByKey("bitcoin-rpcpassword");
    }

    public Optional<Integer> getGrpcPort() {
        return getCommandValueByKey("grpc-port")
                .map(Integer::parseUnsignedInt);
    }

    public String getNetwork() {
        return getCommandValueByKey("network").orElse(DEFAULT_NETWORK);
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

        String rpcuserValue = properties.getBitcoinRpcUser().orElse(null);
        if (rpcuserValue != null) {
            if (rpcuserValue.isBlank()) {
                String errorMessage = "'rpcuser' must not be empty";
                errors.rejectValue("rpcuser", "rpcuser.invalid", errorMessage);
            } else if (containsWhitespaces(rpcuserValue)) {
                String errorMessage = String.format("'rpcuser' must not contain whitespaces - unsupported value: '%s'", rpcuserValue);
                errors.rejectValue("rpcuser", "rpcuser.unsupported", errorMessage);
            }
        }

        String rpcpasswordValue = properties.getBitcoinRpcPassword().orElse(null);
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
        return CharMatcher.whitespace().matchesAnyOf(value);
    }
}

