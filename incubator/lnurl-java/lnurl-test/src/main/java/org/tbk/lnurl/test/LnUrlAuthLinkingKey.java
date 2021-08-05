package org.tbk.lnurl.test;

import com.google.common.collect.ImmutableList;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import fr.acinq.bitcoin.DeterministicWallet.KeyPath;
import fr.acinq.bitcoin.Protocol;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import scala.collection.immutable.Seq;
import scodec.bits.ByteVector;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static fr.acinq.bitcoin.DeterministicWallet.derivePrivateKey;
import static fr.acinq.bitcoin.DeterministicWallet.hardened;
import static scala.collection.JavaConverters.asJava;
import static scala.collection.JavaConverters.asScala;

public class LnUrlAuthLinkingKey {
    // lnurl-auth base path: m/138'
    private static final KeyPath lnurlAuthKeyPathBase = new KeyPath(Seq.newBuilder().result()).derive(hardened(138L));

    // lnurl-auth hashing key path: m/138'/0
    private static final KeyPath lnurlAuthHashingKeyPath = lnurlAuthKeyPathBase.derive(0L);

    public static ExtendedPrivateKey deriveLinkingKey(ExtendedPrivateKey masterPrivateKey, URI domainName) {
        ExtendedPrivateKey hashingKey = derivePrivateKey(masterPrivateKey, lnurlAuthHashingKeyPath);

        KeyPath linkingKeyPath = deriveLinkingKeyPathWithHashingKey(hashingKey.privateKey(), domainName);

        return derivePrivateKey(masterPrivateKey, linkingKeyPath);
    }

    private static KeyPath deriveLinkingKeyPathWithHashingKey(Crypto.PrivateKey hashingKey, URI domainName) {
        return deriveLinkingKeyPathWithHashingKey(hashingKey.value().bytes().toArray(), domainName);
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
        var derivationMaterial = hmac256(ByteVector.view(hashingKey), ByteVector.view(domainBytes));

        var stream = new ByteArrayInputStream(derivationMaterial.slice(0, 16).toArray());

        // each uint32 call consumes next 4 bytes
        List<Long> linkingKeySubPath = ImmutableList.<Long>builder()
                .add(Protocol.uint32(stream, ByteOrder.BIG_ENDIAN))
                .add(Protocol.uint32(stream, ByteOrder.BIG_ENDIAN))
                .add(Protocol.uint32(stream, ByteOrder.BIG_ENDIAN))
                .add(Protocol.uint32(stream, ByteOrder.BIG_ENDIAN))
                .build();

        return derive(lnurlAuthKeyPathBase, linkingKeySubPath);
    }

    private static ByteVector hmac256(final ByteVector key, final ByteVector data) {
        HMac mac = new HMac(new SHA256Digest());
        mac.init(new KeyParameter(key.toArray()));
        mac.update(data.toArray(), 0, (int) data.length());
        byte[] out = new byte[64];
        mac.doFinal(out, 0);
        return ByteVector.view(out);
    }

    private static KeyPath derive(KeyPath keypath, List<Long> subpath) {
        List<Object> path = ImmutableList.builder()
                .addAll(asJava(keypath.path()))
                .addAll(subpath)
                .build();

        return new KeyPath(asScala(path).toSeq());
    }
}
