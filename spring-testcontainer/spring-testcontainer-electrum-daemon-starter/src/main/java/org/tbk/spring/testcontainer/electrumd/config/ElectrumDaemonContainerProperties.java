package org.tbk.spring.testcontainer.electrumd.config;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.testcontainer.electrum-daemon",
        ignoreUnknownFields = false
)
public class ElectrumDaemonContainerProperties implements Validator {
    private static final int DEFAULT_RPC_PORT = 7000;

    /**
     * Whether the client should be enabled
     */
    private boolean enabled;

    /**
     * Specify the port to open for incoming RPC connections.
     */
    private Integer rpcport;

    private List<Integer> exposedPorts;

    public int getRpcPort() {
        return Optional.ofNullable(rpcport).orElse(DEFAULT_RPC_PORT);
    }


    public List<Integer> getExposedPorts() {
        return exposedPorts == null ?
                Collections.emptyList() :
                ImmutableList.copyOf(exposedPorts);
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

