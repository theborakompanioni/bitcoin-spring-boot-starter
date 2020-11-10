package org.tbk.bitcoin.common.util;

// see https://blockchair.com/bitcoin/block/0
public final class GenesisMessage {
    // OP_PUSHBYTES_4 ffff001d OP_PUSHBYTES_1 04 OP_PUSHBYTES_69 5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73
    private static final String MESSAGE = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";

    private static final GenesisMessage INSTANCE = new GenesisMessage();

    public static String message() {
        return MESSAGE;
    }

    public static GenesisMessage get() {
        return INSTANCE;
    }

    private GenesisMessage() {
    }

    @Override
    public String toString() {
        return MESSAGE;
    }
}
