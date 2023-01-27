package org.tbk.spring.testcontainer.tor.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.testcontainers.shaded.com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class TorContainerApplicationTest {
    private static final URI CHECK_TOR_URL = URI.create("https://check.torproject.org/");

    @SpringBootApplication
    public static class TorContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(TorContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired(required = false)
    private TorContainer<?> container;

    @Autowired(required = false)
    private CloseableHttpClient torHttpClient;

    @Test
    void contextLoads() {
        assertThat(container, is(notNullValue()));
        assertThat(container.isRunning(), is(true));
    }

    @Test
    @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
        // we are in control of the request
    void fetchPageWithTor() throws IOException {
        SocketAddress sockAddr = new InetSocketAddress("localhost", container.getMappedPort(9050));
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, sockAddr);
        URL url = CHECK_TOR_URL.toURL();

        try (InputStreamReader r = new InputStreamReader(url.openConnection(proxy).getInputStream(), StandardCharsets.UTF_8)) {
            String body = CharStreams.toString(r);

            assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
            assertThat(body, not(containsStringIgnoringCase("Sorry")));
            assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
        }
    }

    @Test
    void fetchPageWithoutTor() throws IOException {
        URL url = CHECK_TOR_URL.toURL();

        assert url.getHost().equals(CHECK_TOR_URL.getHost()); // spotbugs
        try (InputStreamReader r = new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8)) {
            String body = CharStreams.toString(r);

            assertThat(body, containsString("Sorry. You are not using Tor."));
            assertThat(body, not(containsStringIgnoringCase("Congratulations")));
            assertThat(body, not(containsStringIgnoringCase("This browser is configured to use Tor")));
        }
    }

    @Test
    void fetchPageWithHttpClientOverTor() throws IOException {
        HttpGet req = new HttpGet(CHECK_TOR_URL);

        try (CloseableHttpResponse rsp = torHttpClient.execute(req)) {
            assertThat(rsp, is(notNullValue()));

            String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

            assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
            assertThat(body, not(containsStringIgnoringCase("Sorry")));
            assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
        }
    }
}

