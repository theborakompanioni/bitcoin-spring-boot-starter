package org.tbk.tor.http;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.tor.NativeTorFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class TorHttpClientTest {
    // "onion.torproject.org" as onion. taken from https://onion.torproject.org/ on 2020-01-13
    private static final String ONION_URL = "http://yz7lpwfhhzcdyc5y.on" + "ion/";

    private static final String CHECK_TOR_URL_HTTP = "http://check.torproject.org/";
    private static final String CHECK_TOR_URL_HTTPS = "https://check.torproject.org/";

    private CloseableHttpClient sut;

    private NativeTor nativeTor;

    @BeforeEach
    public void setUp() throws TorCtlException {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        NativeTorFactory torFactory = new NativeTorFactory(workingDirectory);

        this.nativeTor = torFactory.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        HttpClientBuilder torHttpClientBuilder = SimpleTorHttpClientBuilder.tor(this.nativeTor);

        this.sut = torHttpClientBuilder.build();
    }

    @AfterEach
    public void tearDown() {
        try {
            this.sut.close();
        } catch (IOException e) {
            log.warn("Error while closing http client in teardown phase", e);
        }

        this.nativeTor.shutdown();
    }

    @Test
    public void testOnionWithTor() throws IOException {
        HttpGet req = new HttpGet(ONION_URL);

        try (CloseableHttpResponse rsp = this.sut.execute(req)) {
            String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

            // body should contain a list of addresses - including the one we fetched from!
            assertThat(body, containsString("onion.torproject.org"));
            assertThat(body, containsString(ONION_URL));
        }
    }

    @Test
    public void testHttpWithTor() throws IOException {
        List<String> urls = Lists.newArrayList(CHECK_TOR_URL_HTTP, CHECK_TOR_URL_HTTPS);

        for (String url : urls) {
            HttpGet req = new HttpGet(url);

            try (CloseableHttpResponse rsp = this.sut.execute(req)) {
                String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

                assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
                assertThat(body, not(containsStringIgnoringCase("Sorry")));
                assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
            }
        }
    }

    @Test
    public void testOnionWithoutTor() throws IOException {
        HttpGet req = new HttpGet(ONION_URL);

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            try (CloseableHttpResponse ignoredOnPurpose = client.execute(req)) {
                Assertions.fail("Should have thrown exception");
            }
        } catch (UnknownHostException e) {
            assertThat(e.getMessage(), containsString("Name or service not known"));
        }
    }

    @Test
    public void testHttpWithoutTor() throws IOException {
        HttpGet req = new HttpGet(CHECK_TOR_URL_HTTPS);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

            try (CloseableHttpResponse rsp = client.execute(req)) {
                String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

                assertThat(body, containsString("Sorry. You are not using Tor."));
                assertThat(body, not(containsStringIgnoringCase("Congratulations")));
                assertThat(body, not(containsStringIgnoringCase("This browser is configured to use Tor")));
            }
        }
    }
}