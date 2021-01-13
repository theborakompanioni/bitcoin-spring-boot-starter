package org.tbk.tor;

import org.berndpruenster.netlayer.tor.Control;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NativeTorFactoryIntegrationTest {
    // "www.torproject.org" as onion. taken from https://onion.torproject.org/ on 2020-01-13
    private static final String onionUrl = "expyuzz" + "4wqqyqh" + "j" + "n.on" + "ion";

    NativeTorFactory sut;

    @Before
    public void setUp() {
        this.sut = new NativeTorFactory();
    }

    @Test
    public void itShouldCheckOnionUrlAvailabilitySuccessfully() {
        NativeTor nativeTor = sut.create();

        Control control = nativeTor.getControl();

        boolean onionUrlIsAvailable = control.hsAvailable(onionUrl);

        nativeTor.shutdown();

        assertThat("onion url is available", onionUrlIsAvailable, is(true));
    }
}