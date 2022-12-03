package org.tbk.lnurl.simple.auth;

import fr.acinq.bitcoin.PublicKey;
import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SimpleLinkingKeyTest {

    @Test
    void fromHexFailsOnUncompressedPublicKey() {
        String uncompressedValidLinkingKeyHex = "0465d6177992064a24c24213230a0c3eeb5f2047c7286391c7ead608cda473f787af9afcae9af3a6a84f28a775ad257dbf6027448461455aaf482569237dda27bd";

        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> SimpleLinkingKey.fromHex(uncompressedValidLinkingKeyHex));

        assertThat(e.getMessage(), is("data must be a compressed (33-byte) secp256k1 public key"));
    }

    @Test
    void fromHexFailsOnInvalidKey() {
        String validLookingLinkingKey = "02" + "00".repeat(32); // looks valid but is not on sek256k1
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> SimpleLinkingKey.fromHex(validLookingLinkingKey));

        assertThat(e.getMessage(), is("data must be a valid secp256k1 public key"));
    }

    @Test
    void fromHexSuccessWithCompressedValidSecp256k1PublicKey() {
        String uncompressedValidLinkingKeyHex = "0465d6177992064a24c24213230a0c3eeb5f2047c7286391c7ead608cda473f787af9afcae9af3a6a84f28a775ad257dbf6027448461455aaf482569237dda27bd";
        String compressedValidLinkingKeyHex = Hex.encode(PublicKey.compress(Hex.decode(uncompressedValidLinkingKeyHex)));

        assertThat(compressedValidLinkingKeyHex, is("0365d6177992064a24c24213230a0c3eeb5f2047c7286391c7ead608cda473f787"));

        SimpleLinkingKey linkingKey = SimpleLinkingKey.fromHex(compressedValidLinkingKeyHex);

        assertThat(linkingKey.toHex(), is(compressedValidLinkingKeyHex));
        assertThat(linkingKey, is(new SimpleLinkingKey(Hex.decode(compressedValidLinkingKeyHex))));
    }
}