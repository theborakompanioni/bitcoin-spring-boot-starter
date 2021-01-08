package org.tbk.tor;

import com.google.common.io.CharStreams;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Ignore("does not work as expected atm - always returns empty string for body")
public class NativeTorFactoryTest {

    NativeTorFactory sut;

    NativeTor nativeTor;

    @Before
    public void setUp() {
        this.sut = new NativeTorFactory();

        this.nativeTor = sut.create();
    }

    @After
    public void tearDown() {
        this.nativeTor.shutdown();
    }

    @Test
    public void test() throws IOException {
        String host = "check.torproject.org";
        //String host = "www.google.com";
        try (TorSocket torSocket = new TorSocket(host, 80, null, 1, this.nativeTor)) {
            try (InputStreamReader r = new InputStreamReader(torSocket.getInputStream())) {
                String body = CharStreams.toString(r);

                assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
                assertThat(body, not(containsStringIgnoringCase("Sorry")));
                assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
            }
        }
    }
}