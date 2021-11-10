package org.tbk.lnurl.simple.auth;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.secp256k1.Hex;
import org.tbk.lnurl.auth.LinkingKey;

import static com.google.common.base.Preconditions.checkArgument;

public final class SimpleLinkingKey extends AbstractByteArrayView implements LinkingKey {

    public static SimpleLinkingKey fromHex(String hex) {
        return fromBin(Hex.decode(hex));
    }

    public static SimpleLinkingKey fromBin(byte[] data) {
        checkArgument(Crypto.isPubKeyCompressed(data), "data must be a compressed (33-byte) secp256k1 public key");
        checkArgument(Crypto.isPubKeyValid(data), "data must be a valid secp256k1 public key");

        return new SimpleLinkingKey(data);
    }

    SimpleLinkingKey(byte[] data) {
        super(data);
    }
}
