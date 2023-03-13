package org.tbk.bitcoin.common.genesis;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GenesisMessageTest {

    @Test
    void itShouldNotTrustButVerify() {
        // OP_PUSHBYTES_4 ffff001d OP_PUSHBYTES_1 04 OP_PUSHBYTES_69 5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73
        assertThat(HexFormat.of().formatHex(GenesisMessage.get().toByteArray()), is("5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73"));
        assertThat(GenesisMessage.get().text(), is("The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"));
    }
}