package org.tbk.tor.spring.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.berndpruenster.netlayer.tor.Tor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.tor.hs.HiddenServiceDefinition;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TorExampleApplicationTest {

    @Autowired(required = false)
    private Tor tor;

    @Autowired(required = false)
    @Qualifier("torHttpClient")
    private CloseableHttpClient torHttpClient;

    @Autowired(required = false)
    @Qualifier("applicationHiddenServiceDefinition")
    private HiddenServiceDefinition applicationHiddenServiceDefinition;

    @Test
    public void contextLoads() {
        assertThat(tor, is(notNullValue()));
        assertThat(torHttpClient, is(notNullValue()));
        assertThat(applicationHiddenServiceDefinition, is(notNullValue()));
    }

    @Test
    public void itShouldVerifyHiddenServiceIsAvailable() {
        String onionUrl = applicationHiddenServiceDefinition.getVirtualHostOrThrow();

        boolean hiddenServiceAvailable = tor.isHiddenServiceAvailable(onionUrl);
        assertThat("hidden service is available", hiddenServiceAvailable, is(true));
    }

    @Test
    @Ignore("Does not work at the moment.. always times out. Works outside of the tests (e.g. in health checks) - investigate!")
    public void itShouldVerifyServerIsReachableViaOnionUrl() {
        String onionUrl = applicationHiddenServiceDefinition.getVirtualHostOrThrow();
        String url = String.format("http://%s:%d/index.html", onionUrl, applicationHiddenServiceDefinition.getVirtualPort());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout((int) Duration.ofSeconds(10).toMillis())
                .setConnectTimeout((int) Duration.ofSeconds(10).toMillis())
                .setSocketTimeout((int) Duration.ofSeconds(10).toMillis())
                .build();

        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);

        // the hidden service takes a while to be published.
        // poll till the onion url is reachable in the tor network.
        // timeout if it takes too long
        String body  = Flux.interval(Duration.ofSeconds(10))
                .flatMap(i -> {
                    try (CloseableHttpResponse response = torHttpClient.execute(request)) {
                        String entity = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        return Flux.just(entity);
                    } catch (IOException e) {
                        log.debug("Exception while polling for {}: {}", url, e.getMessage());
                        return Flux.empty();
                    }
                })
                .blockFirst(Duration.ofMinutes(3));

        assertThat(body, containsString("<title>Tor Example Application</title>"));
        assertThat(body, containsString("<h1>It works!</h1>"));
    }

}
