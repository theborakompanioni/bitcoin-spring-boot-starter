package org.tbk.spring.testcontainer.electrumd.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.spring.testcontainer.core.AbstractContainerProperties;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.electrum-daemon",
        ignoreUnknownFields = false
)
public class ElectrumDaemonContainerProperties extends AbstractContainerProperties implements Validator {
    private static final int DEFAULT_RPC_PORT = 7000;

    /**
     * Specify the port to open for incoming RPC connections.
     */
    private Integer rpcport;

    /**
     * (Optional) Specify the wallet that electrum should open on startup.
     */
    private String defaultWallet;

    public int getRpcPort() {
        return Optional.ofNullable(rpcport).orElse(DEFAULT_RPC_PORT);
    }

    public Optional<String> getDefaultWallet() {
        return Optional.ofNullable(defaultWallet);
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

    }
}

