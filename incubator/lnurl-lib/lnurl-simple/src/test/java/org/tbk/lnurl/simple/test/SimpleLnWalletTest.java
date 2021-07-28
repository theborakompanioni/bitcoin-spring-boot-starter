package org.tbk.lnurl.simple.test;

import org.junit.jupiter.api.Test;
import org.tbk.lnurl.LnUrlAuth;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SimpleLnWalletTest {

    @Test
    void createLoginUrlFromLnUrlAuthString() {
        SimpleLnService lnService = SimpleLnService.of(URI.create("https://example.com"));

        byte[] seed = new SecureRandom().generateSeed(256);
        SimpleLnWallet lnWallet = SimpleLnWallet.fromSeed(seed);

        LnUrlAuth lnUrlAuth = lnService.createLnUrlAuth();
        URI loginUri = lnWallet.createLoginUri(lnUrlAuth);

        boolean loginVerified = lnService.verifyLogin(loginUri);
        assertThat(loginVerified, is(true));
    }
}
