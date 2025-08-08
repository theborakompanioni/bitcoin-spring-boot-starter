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
    void itShouldFindVanityAddress() throws JsonProcessingException {
        String prefix = "z";

        String result = sut.run(prefix, "", 0, "60s");
        assertThat(result, is(notNullValue()));

        JsonNode json = jsonMapper.readTree(result);
        String address = json.get("address").asText("");
        assertThat(address, startsWith("bc1q" + prefix));

        String mnemonic = json.get("mnemonic").asText("");
        assertThat(mnemonic.split(" ").length, is(12));

        Assertions.assertDoesNotThrow(
                () -> MnemonicCode.validate(mnemonic),
                "mnemonic validation failed"
        );
    }
}
