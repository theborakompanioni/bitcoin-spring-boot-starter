package org.tbk.lnurl.simple.auth;

import fr.acinq.secp256k1.Hex;
import fr.acinq.secp256k1.Secp256k1Exception;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SimpleLinkingKeyTest {

    @Test
    void fromHexStrictSuccess() {
        String validLinkingKey = "0465d6177992064a24c24213230a0c3eeb5f2047c7286391c7ead608cda473f787af9afcae9af3a6a84f28a775ad257dbf6027448461455aaf482569237dda27bd";
        SimpleLinkingKey linkingKey = SimpleLinkingKey.fromHexStrict(validLinkingKey);

        assertThat(linkingKey.toHex(), is(validLinkingKey));
        assertThat(linkingKey, is(new SimpleLinkingKey(Hex.decode(validLinkingKey))));
    }

    @Test
    void fromHexStrictFail() {
        String validLookingLinkingKey = "04" + "00".repeat(64); // looks valid but is not on sek256k1
        Secp256k1Exception e = Assertions.assertThrows(Secp256k1Exception.class, () -> SimpleLinkingKey.fromHexStrict(validLookingLinkingKey));

        assertThat(e.getMessage(), is("secp256k1_ec_pubkey_parse failed"));
    }

    @Test
    void fromHexLaxSuccess() {
        String validLinkingKey = "04c8f8a0c04f84ecfe7bf11d818efe67eeefd5bc25aafac437e03b63ce0aaf8a15c930640941add57453019c45f6000a002d2496aa3f30de8795a75dc1266e651c";
        SimpleLinkingKey linkingKey = SimpleLinkingKey.fromHexLax(validLinkingKey);

        assertThat(linkingKey.toHex(), is(validLinkingKey));
    }

    @Test
    void fromHexLaxSuccessMinimal() {
        String validLookingLinkingKey = "04" + "00".repeat(64); // looks valid but is not on sek256k1
        SimpleLinkingKey linkingKey = SimpleLinkingKey.fromHexLax(validLookingLinkingKey);

        assertThat(linkingKey.toHex(), is(validLookingLinkingKey));
    }
}