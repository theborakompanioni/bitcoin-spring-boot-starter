package org.tbk.lnurl.test;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.Test;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SimpleLnurlWalletTest {

    @Test
    void createLoginUrlFromLnUrlAuthString() {
        SimpleLnurlService lnService = SimpleLnurlService.of(URI.create("https://example.com"));

        byte[] seed = new SecureRandom().generateSeed(256);
        SimpleLnurlWallet lnWallet = SimpleLnurlWallet.fromSeed(seed);

        LnurlAuth lnurlAuth = lnService.createLnUrlAuth();
        SignedLnurlAuth signedLnurlAuth = lnWallet.authorize(lnurlAuth);
        assertThat(Crypto.isPubKeyCompressed(signedLnurlAuth.getLinkingKey().toArray()), is(true));
        assertThat(Crypto.isPubKeyValid(signedLnurlAuth.getLinkingKey().toArray()), is(true));

        boolean loginVerified = lnService.verify(signedLnurlAuth);
        assertThat(loginVerified, is(true));
    }

    @Test
    void verifyCompressedSecp256k1PublicKeyDerivationWithRandomSeed() {
        URI serviceUrl = URI.create("https://example.com");

        byte[] seed = new SecureRandom().generateSeed(256);
        SimpleLnurlWallet lnWallet = SimpleLnurlWallet.fromSeed(seed);

        LinkingKey linkingKey = lnWallet.deriveLinkingPublicKey(serviceUrl);

        assertThat(Crypto.isPubKeyCompressed(linkingKey.toArray()), is(true));
        assertThat(Crypto.isPubKeyValid(linkingKey.toArray()), is(true));
    }

    @Test
    void verifyCompressedSecp256k1PublicKeyDerivationWithStaticSeed() {
        URI serviceUrl = URI.create("https://example.com");

        byte[] seed = Hex.decode("00".repeat(256));
        SimpleLnurlWallet lnWallet = SimpleLnurlWallet.fromSeed(seed);

        LinkingKey linkingKey = lnWallet.deriveLinkingPublicKey(serviceUrl);

        assertThat(linkingKey.toHex(), is("02bcd7093c717597e8f8b67c27b72a96a856067de7efe612cb2633b0234e8bac4f"));
    }
}
