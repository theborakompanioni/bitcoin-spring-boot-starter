package org.tbk.spring.testcontainer.tor.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.testcontainers.shaded.com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TorContainerApplicationTest {
    private static final String CHECK_TOR_URL = "https://check.torproject.org/";

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
    public void contextLoads() {
        assertThat(container, is(notNullValue()));
        assertThat(container.isRunning(), is(true));
    }

    @Test
    public void fetchPageWithTor() throws IOException {
        SocketAddress sockAddr = new InetSocketAddress("localhost", container.getMappedPort(9050));
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, sockAddr);
        URL url = new URL(CHECK_TOR_URL);

        try (InputStreamReader r = new InputStreamReader(url.openConnection(proxy).getInputStream())) {
            String body = CharStreams.toString(r);

            assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
            assertThat(body, not(containsStringIgnoringCase("Sorry")));
            assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
        }
    }

    @Test
    public void fetchPageWithoutTor() throws IOException {
        URL url = new URL(CHECK_TOR_URL);

        try (InputStreamReader r = new InputStreamReader(url.openConnection().getInputStream())) {
            String body = CharStreams.toString(r);

            assertThat(body, containsString("Sorry. You are not using Tor."));
            assertThat(body, not(containsStringIgnoringCase("Congratulations")));
            assertThat(body, not(containsStringIgnoringCase("This browser is configured to use Tor")));
        }
    }

    @Test
    public void fetchPageWithHttpClientOverTor() throws IOException {
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

