package org.tbk.tor.hs;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HiddenServiceDefinitionTest {

    @Test
    void verifyExceptionOnMissingArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> HiddenServiceDefinition.builder().build(),
                "Verify it throws IAE (instead of NPE)");
    }

    @Test
    void verifyExceptionOnMissingFiles() {
        String directory = "./build/tmp/hidden-service-definition-test";
        HiddenServiceDefinition definition = HiddenServiceDefinition.builder()
                .directory(new File(directory))
                .virtualPort(80)
                .port(8080)
                .host(InetAddress.getLoopbackAddress().getHostAddress())
                .build();

        assertThat(definition.getVirtualHost(), is(Optional.empty()));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                definition::getVirtualHostOrThrow,
                "Verify it throws exception as no hostname file is present");

        assertThat(exception.getCause(), instanceOf(NoSuchFileException.class));
        assertThat(exception.getMessage(), containsString(directory + "/hostname"));
    }
}