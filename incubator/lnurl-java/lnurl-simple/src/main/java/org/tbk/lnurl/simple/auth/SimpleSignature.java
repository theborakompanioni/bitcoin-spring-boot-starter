package org.tbk.lnurl.simple.auth;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.secp256k1.Hex;
import org.tbk.lnurl.auth.Signature;
import scodec.bits.ByteVector;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

public final class SimpleSignature extends AbstractByteArrayView implements Signature {

    public static SimpleSignature fromHex(String hex) {
        return new SimpleSignature(Hex.decode(hex));
    }

    SimpleSignature(byte[] data) {
        super(data);
    }

    @Override
    protected void validate(byte[] data) {
        checkArgument(isDERSignature(data), "data must be a DER encoded signature.");
    }

    /**
     * A correct DER-encoded signature has the following format:
     * 0x30 [total-length] 0x02 [R-length] [R] 0x02 [S-length] [S] [sighash]
     * <p>
     * 0x30: a header byte indicating a compound structure.
     * A 1-byte length descriptor for all what follows.
     * 0x02: a header byte indicating an integer.
     * A 1-byte length descriptor for the R value
     * The R coordinate, as a big-endian integer.
     * 0x02: a header byte indicating an integer.
     * A 1-byte length descriptor for the S value.
     * The S coordinate, as a big-endian integer.
     * 1-byte value indicating what data is hashed (not part of the DER signature)
     * <p>
     */
    private boolean isDERSignature(byte[] data) {
        if (data.length < 8) {
            return false;
        }
        if (data.length > 73) {
            return false;
        }
        if (data[0] != 0x30 || data[2] != 0x02) {
            return false;
        }

        boolean sighashAbsent = 2 + data[1] == data.length;
        boolean sighashPresent = 2 + data[1] == data.length - 1;
        if (sighashAbsent) {
            // data has no sighash byte at the end - append one byte and try again.
            return Crypto.isDERSignature(ByteVector.view(Arrays.copyOf(data, data.length + 1)));
            // return Crypto.isDERSignature(ByteVector.view(data).padTo(data.length + 1));
        } else if (sighashPresent) {
            // data has sighash byte at the end
            return Crypto.isDERSignature(ByteVector.view(data));
        } else {
            return false;
        }
    }
}
