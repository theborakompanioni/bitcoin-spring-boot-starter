package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;
import org.testcontainers.shaded.com.google.common.base.CharMatcher;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
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
@Slf4j
public class BitcoindContainerProperties extends AbstractContainerProperties implements Validator {
    private static final String DEFAULT_DOCKER_IMAGE_NAME = "polarlightning/bitcoind:25.0";
    private static final DockerImageName defaultDockerImageName = DockerImageName.parse(DEFAULT_DOCKER_IMAGE_NAME);

    private static final int MAINNET_DEFAULT_RPC_PORT = 8332;
    private static final int MAINNET_DEFAULT_P2P_PORT = 8333;

    private static final int TESTNET_DEFAULT_RPC_PORT = 18332;
    private static final int TESTNET_DEFAULT_P2P_PORT = 18333;

    private static final int REGTEST_DEFAULT_RPC_PORT = 18443;
    private static final int REGTEST_DEFAULT_P2P_PORT = 18444;

    private static final Chain DEFAULT_CHAIN = Chain.regtest;

    @Beta
    private static final List<String> reservedCommands = ImmutableList.<String>builder()
            .add("rpcuser")
            .add("rpcpassword")
            .add("rpcport")
            .add("chain")
            .add("network")
            .add("testnet")
            .add("regtest")
            .build();

    public enum Chain {
        mainnet,
        testnet,
        regtest
    }

    public BitcoindContainerProperties() {
        super(defaultDockerImageName, reservedCommands);
    }

    private Chain chain;

    /**
     * Bitcoin Network
     */
    private String network;
    
    /**
     * RPC port
     */
    private Integer rpcport;
    
    /**
     * P2P Port
     */
    private Integer p2pport;
    
    /**
     * RPC username.
     */
    private String rpcuser;

    /**
     * RPC password.
     */
    private String rpcpassword;

    public Chain getChain() {
        return this.chain != null ? this.chain : DEFAULT_CHAIN;
    }

    public Optional<String> getRpcuser() {
        return Optional.ofNullable(rpcuser);
    }

    public Optional<String> getRpcpassword() {
        return Optional.ofNullable(rpcpassword);
    }
    
    public Integer getRpcport(){
    	Integer port = rpcport;
		log.debug("RPC Port: {}" + this.rpcport);
    	if(port == null) {
    		log.debug("RPC Port not set...using default vaule");
    		port = switch(getNetwork().toLowerCase()) {
	    		case "mainnet" -> MAINNET_DEFAULT_RPC_PORT;
	    		case "testnet" -> TESTNET_DEFAULT_RPC_PORT;
	    		case "regtest" -> REGTEST_DEFAULT_RPC_PORT;
	    		default -> throw new IllegalArgumentException("Invalid network: " + getNetwork());
    		};//getNetwork never returns null, and must be
    		//one of the above options, but need a default anyway
    	}
    	
    	return port;
    }

    public String getNetwork(){
    	
    	log.debug("Network: {}", this.network);
    	return (this.network != null && !this.network.isBlank()) ? this.network : DEFAULT_CHAIN.toString();
    }
    
    public Integer getP2pport() {
    	Integer port = p2pport;
    	log.debug("P2P Port: {}", p2pport);
    	if(port == null) {
    		log.debug("P2P Port not set...providing using default value");
    		port = switch(getNetwork().toLowerCase()) {
	    		case "mainnet" -> MAINNET_DEFAULT_P2P_PORT;
	    		case "testnet" -> TESTNET_DEFAULT_P2P_PORT;
	    		case "regtest" -> REGTEST_DEFAULT_P2P_PORT;
	    		default -> throw new IllegalArgumentException("Invalid network: " + getNetwork());
    		};//getNetwork never returns null, and must be
    		//one of the above options, but need a default anyway
    	}
    	return port;
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

        String networkValue = this.network;
        if (networkValue != null &&
                Arrays.stream(Chain.values()).noneMatch(
                        c -> c.toString().contentEquals(networkValue))) {

            String errorMessage = "'network' must be mainnet, regtest, or testnet - invalid network";
            errors.rejectValue("network", "network.invalid", errorMessage);
        }

        Integer rpcportValue = this.rpcport;
        if (rpcportValue != null && !portIsValid(rpcportValue)) {
        	String errorMessage = "'rpcport' must be in the range 0-65535 - invalid port.";
        	errors.rejectValue("rpcport", "rpcport.invalid", errorMessage);
        }
        
        Integer p2pport = this.p2pport;
        if (p2pport != null && !portIsValid(p2pport)) {
        	String errorMessage = "'p2pport' must be in the range 0-65535 - invalid port.";
        	errors.rejectValue("p2pport", "p2pport.invalid", errorMessage);
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
    
    private boolean portIsValid(Integer port) {
    	return port >= 0 && port <= 65535;
    }
}

