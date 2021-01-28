package org.tbk.tor;

import org.berndpruenster.netlayer.tor.Control;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.Duration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NativeTorFactoryIntegrationTest {
    // "www.torproject.org" as onion. taken from https://onion.torproject.org/ on 2020-01-13
    private static final String onionUrl = "expyuzz" + "4wqqyqh" + "j" + "n.on" + "ion";

    private NativeTorFactory sut;

    @Before
    public void setUp() {
        File workingDirectory = new File("tor-working-dir");
        this.sut = new NativeTorFactory(workingDirectory);
    }

    @Test
    public void itShouldCheckOnionUrlAvailabilitySuccessfully() {
        NativeTor nativeTor = sut.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        Control control = nativeTor.getControl();

        boolean onionUrlIsAvailable = control.hsAvailable(onionUrl);

        nativeTor.shutdown();

        assertThat("onion url is available", onionUrlIsAvailable, is(true));
    }
}