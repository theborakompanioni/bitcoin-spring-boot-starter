package org.tbk.bitcoin.bip85;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.MnemonicCode;
import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DeterministicEntropyTest {
    // testvectors from BIP85
    private static final String mnemonic = "install scatter logic circle pencil average fall shoe quantum disease suspect usage";
    private static final String xprv = "xprv9s21ZrQH143K2LBWUUQRFXhucrQqBpKdRRxNVq2zBqsx8HVqFk2uYo8kmbaLLHRdqtQpUm98uKfu3vca1LqdGhUtyoFnCNkfmXRyPXLjbKb";

    @Test
    void verifyMnemonicToXprv() {
        byte[] seed = MnemonicCode.toSeed(mnemonic, "");

        DeterministicWallet.ExtendedPrivateKey key = DeterministicWallet.generate(seed);
        assertThat(key.path, is(KeyPath.fromPath("m")));

        String rootXprv = DeterministicWallet.encode(key, DeterministicWallet.xprv);
        assertThat(rootXprv, is(xprv));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#test-case-1
    @Test
    void testCase1() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        byte[] entropy = DeterministicEntropy.keyToEntropy(masterBip32RootKey, 0);
        String entropyHex = Hex.encode(entropy);

        assertThat(entropyHex, is("efecfbccffea313214232d29e71563d941229afb4338c21f9517c41aaa0d16f00b83d2a09ef747e7a64e8e2bd5a14869e693da66ce94ac2da570ab7ee48618f7"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#test-case-2
    @Test
    void testCase2() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        byte[] entropy = DeterministicEntropy.keyToEntropy(masterBip32RootKey, 1);
        String entropyHex = Hex.encode(entropy);

        assertThat(entropyHex, is("70c6e3e8ebee8dc4c0dbba66076819bb8c09672527c4277ca8729532ad711872218f826919f6b67218adde99018a6df9095ab2b58d803b5b93ec9802085a690e"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#bip39
    @Test
    void testCaseBip39Words12() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        String mnemonics = String.join(" ", DeterministicEntropy.keyToMnemonic(masterBip32RootKey, 12, 0));
        assertThat(mnemonics, is("girl mad pet galaxy egg matter matrix prison refuse sense ordinary nose"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#bip39
    @Test
    void testCaseBip39Words18() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        String mnemonics = String.join(" ", DeterministicEntropy.keyToMnemonic(masterBip32RootKey, 18, 0));
        assertThat(mnemonics, is("near account window bike charge season chef number sketch tomorrow excuse sniff circle vital hockey outdoor supply token"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#bip39
    @Test
    void testCaseBip39Words24() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        String mnemonics = String.join(" ", DeterministicEntropy.keyToMnemonic(masterBip32RootKey, 24, 0));
        assertThat(mnemonics, is("puppy ocean match cereal symbol another shed magic wrap hammer bulb intact gadget divorce twin tonight reason outdoor destroy simple truth cigar social volcano"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#xprv
    @Test
    void testCaseXprv() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        DeterministicWallet.ExtendedPrivateKey key = DeterministicEntropy.keyToXprv(masterBip32RootKey, 0);

        String keyXprv = DeterministicWallet.encode(key, DeterministicWallet.xprv);
        assertThat(keyXprv, is("xprv9s21ZrQH143K2srSbCSg4m4kLvPMzcWydgmKEnMmoZUurYuBuYG46c6P71UGXMzmriLzCCBvKQWBUv3vPB3m1SATMhp3uEjXHJ42jFg7myX"));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0085.mediawiki#xprv
    @Test
    void testCaseHex() {
        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.ExtendedPrivateKey.decode(xprv, KeyPath.fromPath("")).getSecond();

        byte[] entropy = DeterministicEntropy.keyToHex(masterBip32RootKey, 64, 0);
        assertThat(Hex.encode(entropy), is("492db4698cf3b73a5a24998aa3e9d7fa96275d85724a91e71aa2d645442f878555d078fd1f1f67e368976f04137b1f7a0d19232136ca50c44614af72b5582a5c"));
    }

    // https://github.com/hoganri/bip85-js/blob/main/test.js
    @Test
    void testCase2FromBip85JsBip39MnemonicToEntropyWithPassword() {
        byte[] seed = MnemonicCode.toSeed(mnemonic, "TREZOR");

        DeterministicWallet.ExtendedPrivateKey masterBip32RootKey = DeterministicWallet.generate(seed);

        byte[] entropy = DeterministicEntropy.keyToEntropy(masterBip32RootKey, 0);
        String entropyHex = Hex.encode(entropy);

        assertThat(entropyHex, is("d24cee04c61c4a47751658d078ae9b0cc9550fe43eee643d5c10ac2e3f5edbca757b2bd74d55ff5bcc2b1608d567053660d9c7447ae1eb84b6619282fd391844"));
    }

}
