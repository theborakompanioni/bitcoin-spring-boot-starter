package org.tbk.lnurl.simple.auth;

import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SimpleSignatureTest {

    @Test
    void fromHexSuccessMinimal() {
        String minimalValidSignatureHex = ""
                + "30" // 0x30: a header byte indicating a compound structure.
                + "08" // A 1-byte length descriptor for all what follows.
                + "02" // 0x02: a header byte indicating an integer.
                + "02" // A 1-byte length descriptor for the R value
                + "4242" // The R coordinate, as a big-endian integer.
                + "02" // 0x02: a header byte indicating an integer.
                + "02" // A 1-byte length descriptor for the S value.
                + "4242" // The S coordinate, as a big-endian integer.
                ;
        SimpleSignature signature = SimpleSignature.fromHex(minimalValidSignatureHex);

        assertThat(signature.toHex(), is(minimalValidSignatureHex));
    }

    @Test
    void fromHexSuccess() {
        String validSignatureHex = "304402205b431db079241a09ffdf9ee1079e63518366f21948c7ca2bc7e19b03a2a4a7a60220419f0e9a44e6905f7950defdc090c17ca3464bb97a7db8905be3a527a4d3e538";
        SimpleSignature signature = SimpleSignature.fromHex(validSignatureHex);

        assertThat(signature.toHex(), is(validSignatureHex));
        assertThat(signature, is(new SimpleSignature(Hex.decode(validSignatureHex))));
    }

    @Test
    void fromHexFail() {
        String invalidSignatureHex = "00".repeat(70);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> SimpleSignature.fromHex(invalidSignatureHex));

        assertThat(e.getMessage(), is("data must be a DER encoded signature."));
    }
}