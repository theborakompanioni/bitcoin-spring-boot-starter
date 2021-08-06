package org.tbk.lnurl.test;

import org.junit.jupiter.api.Test;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.auth.LnurlAuth;

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

        boolean loginVerified = lnService.verify(signedLnurlAuth);
        assertThat(loginVerified, is(true));
    }
}
