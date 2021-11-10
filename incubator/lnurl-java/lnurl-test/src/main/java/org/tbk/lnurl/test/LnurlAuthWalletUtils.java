package org.tbk.lnurl.test;

import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.crypto.Pack;
import fr.acinq.bitcoin.io.ByteArrayInput;
import fr.acinq.bitcoin.io.Input;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static fr.acinq.bitcoin.DeterministicWallet.derivePrivateKey;
import static fr.acinq.bitcoin.DeterministicWallet.hardened;

final class LnurlAuthWalletUtils {
    // lnurl-auth base path: m/138'
    private static final KeyPath lnurlAuthKeyPathBase = new KeyPath("").derive(hardened(138L));

    // lnurl-auth hashing key path: m/138'/0
    private static final KeyPath lnurlAuthHashingKeyPath = lnurlAuthKeyPathBase.derive(0L);

    public static ExtendedPrivateKey deriveLinkingKey(ExtendedPrivateKey masterPrivateKey, URI domainName) {
        ExtendedPrivateKey hashingKey = derivePrivateKey(masterPrivateKey, lnurlAuthHashingKeyPath);

        KeyPath linkingKeyPath = deriveLinkingKeyPathWithHashingKey(hashingKey.getPrivateKey(), domainName);

        return derivePrivateKey(masterPrivateKey, linkingKeyPath);
    }

    private static KeyPath deriveLinkingKeyPathWithHashingKey(PrivateKey hashingKey, URI domainName) {
        return deriveLinkingKeyPathWithHashingKey(hashingKey.value.toByteArray(), domainName);
    }

    /**
     * Derive the linking key for a service for a given uri by providing the `hashing key`.
     * The hashing key MUST BE derived from path m/138'/0.
     * Consider using {@link #deriveLinkingKey(ExtendedPrivateKey, URI)} if you are in control of the private key.
     *
     * @param hashingKey the private key of path m/138'/0
     * @param domainName the uri the hostname is taken from for path derivation
     * @return the path for locating the keys for the service located at given uri
     */
    public static KeyPath deriveLinkingKeyPathWithHashingKey(byte[] hashingKey, URI domainName) {
        byte[] domainBytes = domainName.getHost().getBytes(StandardCharsets.UTF_8);
        var derivationMaterial = hmac256(hashingKey, domainBytes);

        var stream = new ByteArrayInput(Arrays.copyOfRange(derivationMaterial, 0, 16));

        // each uint32 call consumes next 4 bytes
        return lnurlAuthKeyPathBase
                .derive(uint32BigEndian(stream))
                .derive(uint32BigEndian(stream))
                .derive(uint32BigEndian(stream))
                .derive(uint32BigEndian(stream));
    }

    private static long uint32BigEndian(Input stream) {
        return Pack.int32BE(stream) & 0xFFFFFFFFL;
    }

    private static byte[] hmac256(final byte[] key, final byte[] data) {
        Mac mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key);
        mac.update(data, 0, data.length);

        try {
            byte[] out = new byte[64];
            mac.doFinal(out, 0);
            return out;
        } catch (ShortBufferException e) {
            throw new IllegalStateException("Error while performing hmac256", e);
        }
    }
}
