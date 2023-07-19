package org.tbk.bitcoin.common.genesis;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// ```shell
// bitcoin-cli getblock $(bitcoin-cli getblockhash 0) 0 | awk '{print substr($0,263,138)}' | xxd -r -p
// > The Times 03/Jan/2009 Chancellor on brink of second bailout for banks
// ```
public final class GenesisMessage {

    private static final GenesisMessage INSTANCE = GenesisMessage.from(GenesisTx.get());

    public static GenesisMessage from(GenesisTx genesisTx) {
        byte[] genesisTxRaw = genesisTx.toByteArray();
        return new GenesisMessage(Arrays.copyOfRange(genesisTxRaw, 50, 50 + 69));
    }

    public static GenesisMessage get() {
        return INSTANCE;
    }

    private final byte[] raw;

    private final String text;

    private GenesisMessage(byte[] raw) {
        this.raw = Arrays.copyOfRange(raw, 0, raw.length);
        this.text = new String(this.raw, StandardCharsets.US_ASCII);
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(raw, 0, raw.length);
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
