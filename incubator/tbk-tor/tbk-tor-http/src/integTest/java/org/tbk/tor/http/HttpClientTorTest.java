package org.tbk.tor.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tbk.tor.NativeTorFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
public class HttpClientTorTest {
    private static final String CHECK_TOR_URL = "https://check.torproject.org/";

    CloseableHttpClient sut;

    NativeTor nativeTor;

    @Before
    public void setUp() throws TorCtlException {
        NativeTorFactory torFactory = new NativeTorFactory();

        this.nativeTor = torFactory.create();

        HttpClientBuilder torHttpClientBuilder = SimpleTorHttpClientBuilder.tor(this.nativeTor);

        this.sut = torHttpClientBuilder.build();
    }

    @After
    public void tearDown() {
        try {
            this.sut.close();
        } catch (IOException e) {
            log.warn("Error while closing http client in teardown phase", e);
        }

        this.nativeTor.shutdown();
    }

    @Test
    public void test() throws IOException, TorCtlException {
        HttpGet req = new HttpGet(CHECK_TOR_URL);

        try (CloseableHttpResponse rsp = this.sut.execute(req)) {
            String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

            assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
            assertThat(body, not(containsStringIgnoringCase("Sorry")));
            assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
        }
    }
}