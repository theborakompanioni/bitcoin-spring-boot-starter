package org.tbk.bitcoin.jsonrpc.actuator.health;

import com.google.common.collect.ImmutableMap;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;

import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
public class BitcoinJsonRpcHealthIndicator extends AbstractHealthIndicator implements HealthContributor {
    private final BitcoinClient client;

    public BitcoinJsonRpcHealthIndicator(BitcoinClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> baseDetails = ImmutableMap.<String, Object>builder()
                .put("network", firstNonNull(client.getNetParams().getId(), "<empty>"))
                .put("server", client.getServerURI())
                .build();

        try {
            builder.withDetails(baseDetails);
            doHealthCheckInternal(builder);
        } catch (Exception e) {
            log.error("Exception while performing bitcoin jsonrpc client health check", e);

            builder.unknown()
                    .withException(e)
                    .withDetails(baseDetails);
        }
    }

    private void doHealthCheckInternal(Health.Builder builder) {
        log.debug("Performing health check with bitcoin jsonrpc client on {}", client.getServerURI());

        try {
            NetworkInfo networkInfo = client.getNetworkInfo();
            builder.up().withDetails(ImmutableMap.<String, Object>builder()
                    .put("networkinfo", networkInfo)
                    .build());
        } catch (JsonRpcStatusException e) {
            log.warn("Exception while performing health check with bitcoin jsonrpc client on {}: {}",
                    client.getServerURI(), e.getMessage());

            builder.down()
                    .withException(e)
                    .withDetails(ImmutableMap.<String, Object>builder()
                            .put("response", firstNonNull(e.response, "<empty>"))
                            .put("httpMessage", firstNonNull(e.httpMessage, "<empty>"))
                            .put("httpCode", e.httpCode)
                            .put("jsonRpcCode", e.jsonRpcCode)
                            .build());
        } catch (Exception e) {
            log.warn("Exception while performing health check with bitcoin jsonrpc client on {}: {}",
                    client.getServerURI(), e.getMessage());

            builder.down()
                    .withException(e);
        }
    }
}
