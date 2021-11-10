package org.tbk.lnurl.test;

import fr.acinq.bitcoin.ByteVector;
import fr.acinq.bitcoin.ByteVector64;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.auth.*;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;
import org.tbk.lnurl.simple.auth.SimpleSignature;
import org.tbk.lnurl.simple.auth.SimpleSignedLnurlAuth;

import java.net.URI;

@Slf4j
public class SimpleLnurlWallet implements LnurlWallet {

    public static SimpleLnurlWallet fromSeed(byte[] seed) {
        return new SimpleLnurlWallet(seed);
    }

    private final ExtendedPrivateKey masterPrivateKey;

    private SimpleLnurlWallet(byte[] seed) {
        this.masterPrivateKey = DeterministicWallet.generate(seed);
    }

    @Override
    public SignedLnurlAuth authorize(LnurlAuth lnurlAuth) {
        K1WithSigAndKey k1WithSigAndKey = authorizeInternal(lnurlAuth);
        return SimpleSignedLnurlAuth.create(lnurlAuth, k1WithSigAndKey.getKey(), k1WithSigAndKey.getSig());
    }

    @Override
    public LinkingKey deriveLinkingPublicKey(URI uri) {
        ExtendedPrivateKey linkingKey = deriveLinkingKey(uri);
        return SimpleLinkingKey.fromHex(linkingKey.getPublicKey().toHex());
    }

    private ExtendedPrivateKey deriveLinkingKey(URI domain) {
        return LnurlAuthWalletUtils.deriveLinkingKey(masterPrivateKey, domain);
    }

    private K1WithSigAndKey authorizeInternal(LnurlAuth lnurlAuth) {
        ExtendedPrivateKey linkingKey = deriveLinkingKey(lnurlAuth.toLnurl().toUri());

        ByteVector64 signedK1 = Crypto.sign(lnurlAuth.getK1().toArray(), linkingKey.getPrivateKey());
        ByteVector signedK1DerEncoded = Crypto.compact2der(signedK1);

        // <LNURL_hostname_and_path>?<LNURL_existing_query_parameters>&sig=<hex(sign(utf8ToBytes(k1), linkingPrivKey))>&key=<hex(linkingKey)>
        String sigParam = signedK1DerEncoded.toHex();
        String keyParam = linkingKey.getPublicKey().toHex();

        return K1WithSigAndKey.builder()
                .k1(lnurlAuth.getK1())
                .sig(SimpleSignature.fromHex(sigParam))
                .key(SimpleLinkingKey.fromHex(keyParam))
                .build();
    }

    @Value
    @Builder
    private static class K1WithSigAndKey {
        @NonNull
        K1 k1;
        @NonNull
        Signature sig;
        @NonNull
        LinkingKey key;
    }
}
