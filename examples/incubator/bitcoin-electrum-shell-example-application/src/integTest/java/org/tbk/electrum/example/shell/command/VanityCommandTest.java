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
class VanityCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testVanityRegtest() throws Exception {
        String command = """
                vanity --network regtest
                """;

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bcrt1q")
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/84'/1'/0'/0/0\"")
                .containsText("}");
    }

    @Test
    void testVanityRegtestPath() throws Exception {
        String command = """
                vanity --network regtest --path m/84'/1'/2'/3/4
                """;

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bcrt1q")
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/84'/1'/2'/3/4/0\"")
                .containsText("}");
    }

    @Test
    void testVanityRegtestAddressType() throws Exception {
        String command = """
                vanity --network regtest --address-type p2tr
                """;

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bcrt1p")
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/86'/1'/0'/0/0\"")
                .containsText("}");
    }

    @Test
    void testVanityPrefix() throws Exception {
        String addressPrefix = "z";
        String command = """
                vanity --address-prefix %s
                """.formatted(addressPrefix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bc1q" + addressPrefix)
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/84'/0'/0'/0/0\"")
                .containsText("}");
    }

    @Test
    void testVanitySuffix() throws Exception {
        String addressSuffix = "z";
        String command = """
                vanity --address-suffix %s
                """.formatted(addressSuffix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bc1q").containsText(addressSuffix + "\"")
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/84'/0'/0'/0/0\"")
                .containsText("}");
    }

    @Test
    void testVanityPrefixAndSuffix() throws Exception {
        String prefixAndSuffix = "z";
        String command = """
                vanity --address-prefix %s --address-suffix %s
                """.formatted(prefixAndSuffix, prefixAndSuffix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"address\" : \"bc1q" + prefixAndSuffix).containsText(prefixAndSuffix + "\"")
                .containsText("\"passphrase\" : \"\"")
                .containsText("\"mnemonic\" : \"")
                .containsText("\"path\" : \"m/84'/0'/0'/0/0\"")
                .containsText("}");
    }
}
