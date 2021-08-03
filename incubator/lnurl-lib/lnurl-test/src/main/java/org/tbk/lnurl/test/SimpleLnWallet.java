package org.tbk.lnurl.test;

import fr.acinq.bitcoin.ByteVector64;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import fr.acinq.secp256k1.Hex;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.Signature;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;
import org.tbk.lnurl.simple.auth.SimpleSignature;
import scodec.bits.ByteVector;

import java.net.URI;

@Slf4j
public class SimpleLnWallet {

    public static SimpleLnWallet fromSeed(byte[] seed) {
        return new SimpleLnWallet(seed);
    }

    private final ExtendedPrivateKey masterPrivateKey;

    private SimpleLnWallet(byte[] seed) {
        this.masterPrivateKey = DeterministicWallet.generate(ByteVector.view(seed));
    }

    private ExtendedPrivateKey deriveLinkingKey(URI domain) {
        return LnUrlAuthLinkingKey.deriveLinkingKey(masterPrivateKey, domain);
    }

    @SneakyThrows
    public URI createLoginUri(LnurlAuth lnUrlAuth) {
        Params params = createParams(lnUrlAuth);

        return new URIBuilder(lnUrlAuth.toUri())
                .setParameter("sig", params.getSig().toHex())
                .setParameter("key", params.getKey().toHex())
                .build();
    }

    @SneakyThrows
    public Params createParams(LnurlAuth lnUrlAuth) {
        ExtendedPrivateKey linkingKey = deriveLinkingKey(lnUrlAuth.toUri());

        ByteVector64 signedK1 = Crypto.sign(lnUrlAuth.getK1().toArray(), linkingKey.privateKey());
        ByteVector signedK1DerEncoded = Crypto.compact2der(signedK1);

        // <LNURL_hostname_and_path>?<LNURL_existing_query_parameters>&sig=<hex(sign(utf8ToBytes(k1), linkingPrivKey))>&key=<hex(linkingKey)>
        String sigParam = Hex.encode(signedK1DerEncoded.toArray());
        String keyParam = Hex.encode(linkingKey.publicKey().value().toArray());

        return Params.builder()
                .k1(lnUrlAuth.getK1())
                .sig(SimpleSignature.fromHex(sigParam))
                .key(SimpleLinkingKey.fromHex(keyParam))
                .build();
    }

    @Value
    @Builder
    public static class Params {
        K1 k1;
        Signature sig;
        LinkingKey key;
    }
}
