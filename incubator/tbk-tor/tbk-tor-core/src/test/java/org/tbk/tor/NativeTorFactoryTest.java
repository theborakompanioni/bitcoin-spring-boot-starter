package org.tbk.tor;

import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public class NativeTorFactoryTest {

    private NativeTorFactory sut;

    @Before
    public void setUp() {
        this.sut = new NativeTorFactory();
    }

    @Test
    public void itShouldCreateTorSuccessfully() {
        NativeTor nativeTor = sut.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        nativeTor.shutdown();
    }
}