package org.tbk.mqtt.moquette.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.moquette.BrokerConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;


@Data
@ConfigurationProperties(
        prefix = "org.tbk.mqtt.moquette.broker",
        ignoreUnknownFields = false
)
public class MoquetteBrokerAutoConfigProperties implements Validator {
    /**
     * Whether the autoconfig should be enabled
     */
    private boolean enabled;

    private String host = BrokerConstants.HOST;

    private int port = BrokerConstants.PORT;

    private Integer websocketPort;

    private Boolean websocketEnabled;

    private Map<String, String> config;

    public Optional<Integer> getWebsocketPort() {
        return Optional.ofNullable(websocketPort);
    }

    public Map<String, String> getConfig() {
        return ImmutableMap.copyOf(firstNonNull(this.config, Collections.emptyMap()));
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == MoquetteBrokerAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        MoquetteBrokerAutoConfigProperties properties = (MoquetteBrokerAutoConfigProperties) target;

        if (Strings.isNullOrEmpty(properties.getHost())) {
            errors.rejectValue("host", "host.invalid", "host must not be empty");
        }

        if (properties.getPort() < 0) {
            String errorMessage = String.format("Port must not be negative - invalid value: %d", properties.getPort());
            errors.rejectValue("port", "port.invalid", errorMessage);
        }

        if (properties.getWebsocketPort().orElse(0) < 0) {
            String errorMessage = String.format("Websocket port must not be negative - invalid value: %d", properties.getWebsocketPort());
            errors.rejectValue("websocketPort", "websocketPort.invalid", errorMessage);
        }
    }
}
