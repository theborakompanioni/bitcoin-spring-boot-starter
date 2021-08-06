package org.tbk.lnurl.test;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import fr.acinq.bitcoin.DeterministicWallet.KeyPath;
import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.Seq;
import scodec.bits.ByteVector;

import java.net.URI;
import java.security.SecureRandom;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class LnurlAuthWalletUtilsTest {

    @Test
    void itShouldCreateLinkingKeyForSiteWithRandomWallet() {
        var domainName = URI.create("https://example.com");
        ExtendedPrivateKey masterPrivateKey = DeterministicWallet.generate(ByteVector.view(new SecureRandom().generateSeed(256)));

        ExtendedPrivateKey linkingKey = LnurlAuthWalletUtils.deriveLinkingKey(masterPrivateKey, domainName);
        assertThat(linkingKey, is(notNullValue()));
    }

    // TEST VECTOR
    // from lnurl-rfc (currently in a pull request - not merged)
    // see https://github.com/fiatjaf/lnurl-rfc/pull/47
    //
    // domain name: site.com
    // hashingPrivKey: 0x7d417a6a5e9a6a4a879aeaba11a11838764c8fa2b959c242d43dea682b3e409b01
    // pathSuffix: Vector(3751473387, 2829804099, 4228872783, 4134047485)
    // full path: m/138'/1603989739'/682320451'/2081389135'/1986563837'
    //
    @Test
    void itShouldCreateLinkingKeyPathWithTestVectors() {
        var domainName = URI.create("https://site.com");
        var hashingKey = Hex.decode("7d417a6a5e9a6a4a879aeaba11a11838764c8fa2b959c242d43dea682b3e409b01");

        // expected path: m/138'/1603989739'/682320451'/2081389135'/1986563837'
        KeyPath expectedKeyPath = new KeyPath(Seq.newBuilder().result())
                .derive(hardened(138L))
                .derive(3751473387L)
                .derive(2829804099L)
                .derive(4228872783L)
                .derive(4134047485L);

        KeyPath linkingKeyPath = LnurlAuthWalletUtils.deriveLinkingKeyPathWithHashingKey(hashingKey, domainName);

        assertThat(linkingKeyPath, is(expectedKeyPath));
        assertThat(linkingKeyPath.toString(), is("m/138'/1603989739'/682320451'/2081389135'/1986563837'"));
    }

    @Test
    void itShouldCreateLinkingKeyForSiteWithGivenWallet() {
        var domainName = URI.create("https://example.com");
        var walletSeed = Hex.decode("00".repeat(512));

        // expected path: m/138'/443735582/1653120675'/1066334360'/230068797'
        KeyPath expectedKeyPath = new KeyPath(Seq.newBuilder().result())
                .derive(hardened(138L))
                .derive(443735582L)
                .derive(hardened(1653120675L))
                .derive(hardened(1066334360L))
                .derive(hardened(230068797L));

        ExtendedPrivateKey masterPrivateKey = DeterministicWallet.generate(ByteVector.view(walletSeed));

        ExtendedPrivateKey linkingKey = LnurlAuthWalletUtils.deriveLinkingKey(masterPrivateKey, domainName);

        assertThat(linkingKey.path(), is(expectedKeyPath));
        assertThat(linkingKey.path().toString(), is("m/138'/443735582/1653120675'/1066334360'/230068797'"));
    }
}
