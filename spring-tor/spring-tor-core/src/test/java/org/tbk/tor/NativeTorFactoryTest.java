package org.tbk.tor;

import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.Duration;

public class NativeTorFactoryTest {

    private NativeTorFactory sut;

    @Before
    public void setUp() {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        this.sut = new NativeTorFactory(workingDirectory);
    }

    @Test
    public void itShouldCreateTorSuccessfully() {
        NativeTor nativeTor = sut.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        nativeTor.shutdown();
    }
}