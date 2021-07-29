package org.tbk.lnurl.simple;

import fr.acinq.secp256k1.Hex;
import lombok.EqualsAndHashCode;
import org.tbk.lnurl.K1;

import java.security.SecureRandom;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class SimpleK1 implements K1 {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static SimpleK1 random() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return new SimpleK1(bytes);
    }

    public static SimpleK1 fromHex(String hex) {
        return new SimpleK1(Hex.decode(hex));
    }

    @EqualsAndHashCode.Include
    private final byte[] data;

    SimpleK1(byte[] data) {
        checkArgument(data.length == 32, "data must be an array of size 32");
        this.data = Arrays.copyOf(data, 32);
    }

    @Override
    public byte[] data() {
        return Arrays.copyOf(data, 32);
    }

    @Override
    public String hex() {
        return Hex.encode(data);
    }
}
