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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.tor.NativeTorFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class TorHttpClientTest {
    // "onion.torproject.org" as onion. taken from https://onion.torproject.org/ on 2022-07-01
    private static final URI ONION_URL = URI.create("http://xao2lxsmia2edq2n5zxg6uahx6xox2t7bfjw6b5vdzsxi7ezmqob6qid.on" + "ion/");

    private static final URI CHECK_TOR_URL_HTTP = URI.create("http://check.torproject.org/");
    private static final URI CHECK_TOR_URL_HTTPS = URI.create("https://check.torproject.org/");

    private CloseableHttpClient sut;

    private NativeTor nativeTor;

    @BeforeEach
    void setUp() throws TorCtlException {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        NativeTorFactory torFactory = new NativeTorFactory(workingDirectory);

        this.nativeTor = torFactory.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        HttpClientBuilder torHttpClientBuilder = SimpleTorHttpClientBuilder.tor(this.nativeTor);

        this.sut = torHttpClientBuilder.build();
    }

    @AfterEach
    void tearDown() {
        try {
            this.sut.close();
        } catch (IOException e) {
            log.warn("Error while closing http client in teardown phase", e);
        }

        this.nativeTor.shutdown();
    }

    @Test
    void testOnionWithTor() throws IOException {
        HttpGet req = new HttpGet(ONION_URL);

        try (CloseableHttpResponse rsp = this.sut.execute(req)) {
            String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

            // body should contain a list of addresses - including the one we fetched from!
            assertThat(body, containsString("onion.torproject.org"));
            assertThat(body, containsString(ONION_URL.toString()));
        }
    }

    @Test
    void testHttpWithTor() throws IOException {
        List<URI> urls = Lists.newArrayList(CHECK_TOR_URL_HTTP, CHECK_TOR_URL_HTTPS);

        for (URI url : urls) {
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
    void testOnionWithoutTor() {
        HttpGet req = new HttpGet(ONION_URL);

        UnknownHostException expectedException = assertThrows(UnknownHostException.class, () -> {
            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                try (CloseableHttpResponse ignoredOnPurpose = client.execute(req)) {
                    fail("Should have thrown exception");
                }
            }
        });

        assertThat(expectedException.getMessage(), containsString("Name or service not known"));
    }

    @Test
    void testHttpWithoutTor() throws IOException {
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