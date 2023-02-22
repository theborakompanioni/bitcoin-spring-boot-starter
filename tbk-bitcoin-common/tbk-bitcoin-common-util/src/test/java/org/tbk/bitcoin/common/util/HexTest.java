package org.tbk.bitcoin.common.util;

import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.common.genesis.GenesisMessage;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HexTest {

    @Test
    void itShouldDecodeBase16_0() {
        byte[] bytes = Hex.decode("cafebabe");
        assertThat(bytes, is(new byte[]{-54, -2, -70, -66}));

        String encoded = Hex.encode(bytes);
        assertThat(encoded, is("cafebabe"));
    }

    @Test
    void itShouldDecodeBase16_1() {
        byte[] bytes = Hex.decode("5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
        assertThat(bytes, is(new byte[]{84, 104, 101, 32, 84, 105, 109, 101, 115, 32, 48, 51, 47, 74, 97, 110, 47, 50, 48, 48, 57, 32, 67, 104, 97, 110, 99, 101, 108, 108, 111, 114, 32, 111, 110, 32, 98, 114, 105, 110, 107, 32, 111, 102, 32, 115, 101, 99, 111, 110, 100, 32, 98, 97, 105, 108, 111, 117, 116, 32, 102, 111, 114, 32, 98, 97, 110, 107, 115}));

        String encoded = Hex.encode(bytes);
        assertThat(encoded, is("5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73"));
    }

    @Test
    void itShouldDecodeBase16CaseInsensitive() {
        byte[] bytes = Hex.decode("CaFeBaBe");
        assertThat(bytes, is(new byte[]{-54, -2, -70, -66}));

        String encoded = Hex.encode(bytes);
        assertThat(encoded, is("cafebabe"));

        assertThat(Hex.decode("abcdef"), is(Hex.decode("ABCDEF")));
        assertThat(Hex.decode("abcdef"), is(Hex.decode("AbCdEf")));
        assertThat(Hex.decode("aBcDeF"), is(Hex.decode("AbCdEf")));
        assertThat(Hex.decode("abcDEF"), is(Hex.decode("ABCdef")));
    }

    @Test
    void itShouldEncodeBase16_1() {
        String hex = Hex.encode(new byte[]{-54, -2, -70, -66});
        assertThat(hex, is("cafebabe"));

        byte[] bytes = Hex.decode(hex);
        assertThat(bytes, is(new byte[]{-54, -2, -70, -66}));
    }

    @Test
    void itShouldEncodeBase16_2() {
        String hex = Hex.encode(GenesisMessage.get().text().getBytes(StandardCharsets.US_ASCII));

        assertThat(hex, is("5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73"));
    }
}
