package org.tbk.lnurl.simple.auth;

import fr.acinq.secp256k1.Hex;
import lombok.EqualsAndHashCode;
import org.tbk.lnurl.auth.ByteArrayView;

import java.util.Arrays;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
abstract class AbstractByteArrayView implements ByteArrayView {

    @EqualsAndHashCode.Include
    private final byte[] data;

    private transient String hex;

    AbstractByteArrayView(byte[] data) {
        validate(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    protected void validate(byte[] data) {
        // empty on purpose - subclasses may override
    }

    public byte[] toArray() {
        return Arrays.copyOf(data, data.length);
    }

    public String toHex() {
        if (hex == null) {
            hex = Hex.encode(data);
        }
        return hex;
    }
}
