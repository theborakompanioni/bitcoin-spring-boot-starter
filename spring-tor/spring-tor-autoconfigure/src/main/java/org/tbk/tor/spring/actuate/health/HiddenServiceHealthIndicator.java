package org.tbk.tor.spring.actuate.health;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class HiddenServiceHealthIndicator extends AbstractHealthIndicator implements HealthContributor {
    private final HiddenServiceDefinition hiddenService;
    private final CloseableHttpClient torHttpClient;

    public HiddenServiceHealthIndicator(HiddenServiceDefinition hiddenService, CloseableHttpClient torHttpClient) {
        this.hiddenService = requireNonNull(hiddenService);
        this.torHttpClient = requireNonNull(torHttpClient);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> baseDetails = ImmutableMap.<String, Object>builder()
                .put("name", hiddenService.getName())
                .put("virtual_host", hiddenService.getVirtualHost().orElse("unknown"))
                .put("virtual_port", hiddenService.getVirtualPort())
                .put("host", hiddenService.getHost())
                .put("port", hiddenService.getPort())
                .build();

        try {
            builder.withDetails(baseDetails);
            doHealthCheckInternal(builder);
        } catch (Exception e) {
            log.error("Exception while performing hidden service health check", e);

            builder.unknown()
                    .withException(e)
                    .withDetails(baseDetails);
        }
    }

    @SneakyThrows(URISyntaxException.class)
    private void doHealthCheckInternal(Health.Builder builder) {
        Optional<String> virtualHost = hiddenService.getVirtualHost();

        if (virtualHost.isEmpty()) {
            log.warn("Cannot perform health check on hidden service {}: Virtual host cannot be read", hiddenService.getName());

            builder.unknown();
            return;
        }

        log.debug("Performing health check on {}", hiddenService);

        URI url = new URIBuilder(virtualHost.get())
                .setScheme("http")
                .setHost(virtualHost.get())
                .setPort(hiddenService.getVirtualPort())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout((int) Duration.ofSeconds(10).toMillis())
                .setConnectTimeout((int) Duration.ofSeconds(10).toMillis())
                .setSocketTimeout((int) Duration.ofSeconds(10).toMillis())
                .setRedirectsEnabled(false)
                .build();

        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);

        try (CloseableHttpResponse response = torHttpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());

            builder.up().withDetails(ImmutableMap.<String, Object>builder()
                    .put("status_code", response.getStatusLine().getStatusCode())
                    .put("reason_phrase", response.getStatusLine().getReasonPhrase())
                    .build());
        } catch (IOException e) {
            log.warn("Exception while performing hidden service health check: {}", e.getMessage());

            builder.down()
                    .withException(e);
        }
    }
}
