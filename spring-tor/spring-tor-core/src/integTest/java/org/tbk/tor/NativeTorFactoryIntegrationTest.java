package org.tbk.tor;

import org.berndpruenster.netlayer.tor.Control;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NativeTorFactoryIntegrationTest {
    // "www.torproject.org" as onion. taken from https://onion.torproject.org/ on 2022-07-01
    private static final String onionUrl = "xao2lxsmia2edq2n5zxg6uahx6xox2t7bfjw6b5vdzsxi7ezmqob6qid.on" + "ion";

    private NativeTorFactory sut;

    @BeforeEach
    public void setUp() {
        File workingDirectory = new File("build/tmp/tor-working-dir");
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