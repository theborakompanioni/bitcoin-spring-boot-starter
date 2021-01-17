package org.tbk.tor.spring.health;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.TorSocket;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class HiddenServiceSocketHealthIndicator extends AbstractHealthIndicator implements HealthContributor {
    private static final String STREAM_ID = "health";

    private final HiddenServiceSocket hiddenServiceSocket;

    public HiddenServiceSocketHealthIndicator(HiddenServiceSocket hiddenServiceSocket) {
        this.hiddenServiceSocket = requireNonNull(hiddenServiceSocket);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> details = ImmutableMap.<String, Object>builder()
                .put("name", hiddenServiceSocket.getServiceName())
                .put("address", hiddenServiceSocket.getSocketAddress())
                .put("local_address", hiddenServiceSocket.getLocalSocketAddress())
                .build();

        log.debug("Performing health check on {}", hiddenServiceSocket);

        try (TorSocket s1 = new TorSocket(hiddenServiceSocket.getSocketAddress(), STREAM_ID)) {
            log.debug("Successfully performed health check on {}", hiddenServiceSocket);

            builder.up().withDetails(details);
        } catch (Exception e) {
            log.warn("Exception while performing hidden service health check: {}", e.getMessage());

            builder.outOfService()
                    .withException(e)
                    .withDetails(details);
        }
    }
}
