package org.tbk.lnurl.simple.auth;

import fr.acinq.secp256k1.Hex;
import org.tbk.lnurl.auth.K1;

import static com.google.common.base.Preconditions.checkArgument;

public final class SimpleK1 extends AbstractByteArrayView implements K1 {

    public static SimpleK1 fromHex(String hex) {
        return fromBytes(Hex.decode(hex));
    }

    public static SimpleK1 fromBytes(byte[] hex) {
        return new SimpleK1(hex);
    }

    SimpleK1(byte[] data) {
        super(data);
    }

    @Override
    protected void validate(byte[] data) {
        checkArgument(data.length == 32, "data must be an array of size 32");
    }
}
