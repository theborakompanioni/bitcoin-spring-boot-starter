package org.tbk.lnurl.simple.auth;

import fr.acinq.secp256k1.Hex;
import org.tbk.lnurl.auth.K1;

import java.security.SecureRandom;

import static com.google.common.base.Preconditions.checkArgument;

public final class SimpleK1 extends AbstractByteArrayView implements K1 {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static SimpleK1 random() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return new SimpleK1(bytes);
    }

    public static SimpleK1 fromHex(String hex) {
        return new SimpleK1(Hex.decode(hex));
    }

    SimpleK1(byte[] data) {
        super(data);
    }

    @Override
    protected void validate(byte[] data) {
        checkArgument(data.length == 32, "data must be an array of size 32");
    }
}
