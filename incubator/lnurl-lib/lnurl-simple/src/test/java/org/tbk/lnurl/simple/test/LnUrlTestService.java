package org.tbk.lnurl.simple.test;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import scodec.bits.ByteVector;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LnUrlTestService {

    public static void main(String[] args) {
        var domainName = URI.create("https://example.com");
        ExtendedPrivateKey masterPrivateKey = DeterministicWallet.generate(ByteVector.view(new SecureRandom().generateSeed(256)));

        ExtendedPrivateKey linkingKey = LnUrlAuthLinkingKey.deriveLinkingKey(masterPrivateKey, domainName);
        assertThat(linkingKey, is(notNullValue()));
    }
}
