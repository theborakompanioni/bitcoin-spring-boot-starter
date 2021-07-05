package org.tbk.bitcoin.common.bitcoinj.util;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MoreHdKeyDerivationTest {

    private static final NetworkParameters network = MainNetParams.get();

    private static final String mnemonicCode = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    private static final List<String> mnemonicCodeList = Arrays.asList(mnemonicCode.split(" "));

    private static final String xpriv = "xprv9s21ZrQH143K3GJpoapnV8SFfukcVBSfeCficPSGfubmSFDxo1kuHnLisriDvSnRRuL2Qrg5ggqHKNVpxR86QEC8w35uxmGoggxtQTPvfUu";
    private static final String xpub = "xpub661MyMwAqRbcFkPHucMnrGNzDwb6teAX1RbKQmqtEF8kK3Z7LZ59qafCjB9eCRLiTVG3uxBxgKvRgbubRhqSKXnGGb1aoaqLrpMBDrVxga8";

    @BeforeEach
    public void setup() {
    }

    @Test
    public void itShouldCreateMainnetBech32AddressFromMnemonic() {
        byte[] seed = MnemonicCode.toSeed(mnemonicCodeList, "");

        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        assertThat(masterPrivateKey.getPathAsString(), is("M"));

        DeterministicKey xprivChildBip84 = MoreHdKeyDerivation.deriveChildKey(masterPrivateKey, "84H / 0H / 0H / 0 / 0");
        assertThat(xprivChildBip84.getPathAsString(), is("M/84H/0H/0H/0/0"));

        Address address = Address.fromKey(network, xprivChildBip84, Script.ScriptType.P2WPKH);
        assertThat(address.toString(), is("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"));
    }

    @Test
    public void itShouldCreateMainnetBech32AddressFromXpriv() {
        DeterministicKey xprivKey = DeterministicKey.deserializeB58(xpriv, network);
        assertThat(xprivKey.getPathAsString(), is("M"));

        DeterministicKey xprivChildBip84 = MoreHdKeyDerivation.deriveChildKey(xprivKey, "84H / 0H / 0H / 0 / 0");
        assertThat(xprivChildBip84.getPathAsString(), is("M/84H/0H/0H/0/0"));

        Address address = Address.fromKey(network, xprivChildBip84, Script.ScriptType.P2WPKH);
        assertThat(address.toString(), is("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"));
    }

    @Test
    public void itShouldDeriveAddressesFromZpriv() {
        // the zpriv of the path "84H / 0H / 0H" of mnemonic "abendon abendon ... about"
        String zprivBip84 = "zprvAdG4iTXWBoARxkkzNpNh8r6Qag3irQB8PzEMkAFeTRXxHpbF9z4QgEvBRmfvqWvGp42t42nvgGpNgYSJA9iefm1yYNZKEm7z6qUWCroSQnE";

        DeterministicKey zprivBip84Key = DeterministicKey.deserializeB58(zprivBip84, network);
        assertThat(zprivBip84Key.getPathAsString(), is("M/0H"));

        DeterministicKey zprivBip84KeyChild = MoreHdKeyDerivation.deriveChildKey(zprivBip84Key, "0 / 0");
        assertThat(zprivBip84KeyChild.getPathAsString(), is("M/0H/0/0"));

        Address address = Address.fromKey(network, zprivBip84KeyChild, Script.ScriptType.P2WPKH);
        assertThat(address.toString(), is("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"));
    }

    @Test
    public void itShouldDeriveHardenedAddressesFromZpriv() {
        // the zpriv of the path "84H / 0H / 0H" of mnemonic "abendon abendon ... about"
        String zprivBip84 = "zprvAdG4iTXWBoARxkkzNpNh8r6Qag3irQB8PzEMkAFeTRXxHpbF9z4QgEvBRmfvqWvGp42t42nvgGpNgYSJA9iefm1yYNZKEm7z6qUWCroSQnE";

        DeterministicKey zprivBip84Key = DeterministicKey.deserializeB58(zprivBip84, network);
        assertThat(zprivBip84Key.getPathAsString(), is("M/0H"));

        DeterministicKey xprivBip84KeyChild = MoreHdKeyDerivation.deriveChildKey(zprivBip84Key, "0H");
        assertThat(xprivBip84KeyChild.getPathAsString(), is("M/0H/0H"));

        Address address = Address.fromKey(network, xprivBip84KeyChild, Script.ScriptType.P2WPKH);
        assertThat(address.toString(), is("bc1qfwuage805chdj3lykds088hgn7j47sundvhzr8"));
    }

    @Test
    public void itShouldDeriveAddressesFromZpub() {
        // zpub of "84H / 0H / 0H" of mnemonic "abendon abendon ... about"
        String zpubBip84 = "zpub6rFR7y4Q2AijBEqTUquhVz398htDFrtymD9xYYfG1m4wAcvPhXNfE3EfH1r1ADqtfSdVCToUG868RvUUkgDKf31mGDtKsAYz2oz2AGutZYs";

        DeterministicKey zpubBip84Key = DeterministicKey.deserializeB58(zpubBip84, network);
        assertThat(zpubBip84Key.getPathAsString(), is("M/0H"));

        DeterministicKey zpubBip84KeyChild = MoreHdKeyDerivation.deriveChildKey(zpubBip84Key, "0 / 0");
        assertThat(zpubBip84KeyChild.getPathAsString(), is("M/0H/0/0"));

        Address address = Address.fromKey(network, zpubBip84KeyChild, Script.ScriptType.P2WPKH);
        assertThat(address.toString(), is("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"));
    }

    @Test
    public void itShouldFailToDeriveHardenedAddressesFromZpub() {
        // zpub of "84H / 0H / 0H" of mnemonic "abendon abendon ... about"
        String zpubBip84 = "zpub6rFR7y4Q2AijBEqTUquhVz398htDFrtymD9xYYfG1m4wAcvPhXNfE3EfH1r1ADqtfSdVCToUG868RvUUkgDKf31mGDtKsAYz2oz2AGutZYs";

        DeterministicKey zpubBip84Key = DeterministicKey.deserializeB58(zpubBip84, network);
        try {
            DeterministicKey ignoredOnPurpose = MoreHdKeyDerivation.deriveChildKey(zpubBip84Key, "0H");

            Assertions.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Hardened derivation is unsupported (0H)."));
        }
    }
}