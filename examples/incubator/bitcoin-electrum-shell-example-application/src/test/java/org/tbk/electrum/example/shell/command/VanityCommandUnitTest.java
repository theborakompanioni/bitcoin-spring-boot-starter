package org.tbk.electrum.example.shell.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import fr.acinq.bitcoin.MnemonicCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class VanityCommandUnitTest {

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    private static final VanityCommand sut = new VanityCommand(jsonMapper);

    @Test
    void itShouldFindVanityAddressMainnetPrefixSimple() throws JsonProcessingException {
        String prefix = "z";

        String result = sut.run(prefix, "", "", "", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("bc1q" + prefix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }

    @Test
    void itShouldFindVanityAddressMainnetSuffixSimple() throws JsonProcessingException {
        String suffix = "z";

        String result = sut.run("", suffix, "", "", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("bc1q"));
        assertThat(address, endsWith(suffix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }

    @Test
    void itShouldFindVanityAddressMainnetTimeoutError() {
        String suffix = "zzzzzzzzzzzzzzz";

        Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            sut.run("", suffix, "", "", "", 0, "1ns");
        });
        assertThat(exception.getMessage(), is("Timeout on blocking read for 1 NANOSECONDS"));
    }

    @Test
    void itShouldFindVanityAddressMainnetP2pkhSimple() throws JsonProcessingException {
        String prefix = "A";

        String result = sut.run(prefix, "", "", "p2pkh", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("1" + prefix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }

    @Test
    void itShouldFindVanityAddressMainnetP2shSimple() throws JsonProcessingException {
        String prefix = "A";

        String result = sut.run(prefix, "", "", "p2wpkh_p2sh", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("3" + prefix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }

    @Test
    void itShouldFindVanityAddressRegtestSimple() throws JsonProcessingException {
        String prefix = "z";

        String result = sut.run(prefix, "", "regtest", "", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("bcrt1q" + prefix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }

    @Test
    void itShouldFindVanityAddressRegtestP2pkhSimple() throws JsonProcessingException {
        String prefix = "y";

        String result = sut.run(prefix, "", "regtest", "p2pkh", "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, either(startsWith("m" + prefix)).or(startsWith("n" + prefix)));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(() -> MnemonicCode.validate(mnemonic), "mnemonic validation failed");
    }
}
