package org.tbk.lnurl.simple.auth;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.secp256k1.Hex;
import org.tbk.lnurl.auth.LinkingKey;
import scodec.bits.ByteVector;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

public final class SimpleLinkingKey extends AbstractByteArrayView implements LinkingKey {

    public static SimpleLinkingKey fromHex(String hex) {
        return fromHex(hex, Crypto::isPubKeyValidLax);
    }

    public static SimpleLinkingKey fromHexStrict(String hex) {
        return fromHex(hex, Crypto::isPubKeyValidStrict);
    }

    private static SimpleLinkingKey fromHex(String hex, Function<ByteVector, Boolean> validator) {
        byte[] data = Hex.decode(hex);
        checkArgument(validator.apply(ByteVector.view(data)), "data must be a valid secp256k1 public key.");

        return new SimpleLinkingKey(data);
    }

    SimpleLinkingKey(byte[] data) {
        super(data);
    }
}
