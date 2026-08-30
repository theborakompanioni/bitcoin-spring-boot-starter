package org.tbk.electrum.example.shell.command;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellScreen;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.tbk.electrum.example.shell.ElectrumShellExampleApplication;

@Slf4j
@ShellTest
@SpringBootTest(classes = ElectrumShellExampleApplication.class)
class InfoCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testInfo() throws Exception {
        ShellScreen screen = client.sendCommand("getinfo");

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"network\" : \"regtest\"")
                .containsText("\"path\" : \"/home/electrum/.electrum/regtest\"")
                .containsText("\"server\" : \"host.testcontainers.internal\"")
                .containsText("\"blockchain_height\" : ")
                .containsText("\"server_height\" : ")
                .containsText("\"spv_nodes\" : ")
                .containsText("\"connected\" : ")
                .containsText("\"auto_connect\" : ")
                .containsText("\"version\" : \"")
                .containsText("\"fee_estimates\" : {")
                .containsText("}");
    }
}
