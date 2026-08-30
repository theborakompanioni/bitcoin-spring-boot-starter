package org.tbk.electrum.example.shell;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellScreen;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;

@Slf4j
@ShellTest
@SpringBootTest(classes = ElectrumShellExampleApplication.class)
class ElectrumShellExampleApplicationShellTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void interactiveShellTest() throws Exception {
        ShellScreen screen = client.sendCommand("help");
        ShellAssertions.assertThat(screen).containsText("AVAILABLE COMMANDS");
    }
}
