package org.tbk.electrum.example.shell.opt;

import fr.acinq.bitcoin.Block;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.tbk.electrum.example.shell.util.Wallet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WalletTest {

    @Nested
    class WalletMainnetTest {
        private final Wallet sut = Wallet.from(Block.LivenetGenesisBlock, Wallet.Mnemonic.builder()
                .mnemonic("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about")
                .passphrase("")
                .build());

        @Test
        void p2pkh() {
            Wallet.AddressAndPath address0 = sut.p2pkh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/44'/0'/0'/0/0"));
            assertThat(address0.getAddress(), is("1LqBGSKuX5yYUonjxT5qGfpUsXKYYWeabA"));

            Wallet.AddressAndPath address21 = sut.p2pkh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/44'/0'/0'/0/21"));
            assertThat(address21.getAddress(), is("147wF1v295V9YqmFJxFHrQB7M2GE2Esnb5"));
        }

        @Test
        void p2sh() {
            Wallet.AddressAndPath address0 = sut.p2sh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/49'/0'/0'/0/0"));
            assertThat(address0.getAddress(), is("37VucYSaXLCAsxYyAPfbSi9eh4iEcbShgf"));

            Wallet.AddressAndPath address21 = sut.p2sh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/49'/0'/0'/0/21"));
            assertThat(address21.getAddress(), is("3CLrbqrkmXnMtzJcfXLr9R4zVEVTkASDDv"));
        }

        @Test
        void p2wpkh() {
            Wallet.AddressAndPath address0 = sut.p2wpkh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/84'/0'/0'/0/0"));
            assertThat(address0.getAddress(), is("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"));

            Wallet.AddressAndPath address21 = sut.p2wpkh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/84'/0'/0'/0/21"));
            assertThat(address21.getAddress(), is("bc1q7ynxq7vj5uevr243zalsyguttmn636wh7dkml0"));
        }
    }

    @Nested
    class WalletRegtestTest {
        private final Wallet sut = Wallet.from(Block.RegtestGenesisBlock, Wallet.Mnemonic.builder()
                .mnemonic("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about")
                .passphrase("")
                .build());

        @Test
        void p2pkh() {
            Wallet.AddressAndPath address0 = sut.p2pkh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/44'/1'/0'/0/0"));
            assertThat(address0.getAddress(), is("mkpZhYtJu2r87Js3pDiWJDmPte2NRZ8bJV"));

            Wallet.AddressAndPath address21 = sut.p2pkh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/44'/1'/0'/0/21"));
            assertThat(address21.getAddress(), is("mm1y1pX8EnCC6aBZeeubC8Li9N7oShXDm1"));
        }

        @Test
        void p2sh() {
            Wallet.AddressAndPath address0 = sut.p2sh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/49'/1'/0'/0/0"));
            assertThat(address0.getAddress(), is("2Mww8dCYPUpKHofjgcXcBCEGmniw9CoaiD2"));

            Wallet.AddressAndPath address21 = sut.p2sh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/49'/1'/0'/0/21"));
            assertThat(address21.getAddress(), is("2Mx9KKc2kVjupYG7TegupBVBWRwquxPCgcV"));
        }

        @Test
        void p2wpkh() {
            Wallet.AddressAndPath address0 = sut.p2wpkh().take(1).toStream().findFirst().orElseThrow();
            assertThat(address0.getKeyPath().toString(), is("m/84'/1'/0'/0/0"));
            assertThat(address0.getAddress(), is("bcrt1q6rz28mcfaxtmd6v789l9rrlrusdprr9pz3cppk"));

            Wallet.AddressAndPath address21 = sut.p2wpkh().skip(21).take(1).toStream().findFirst().orElseThrow();
            assertThat(address21.getKeyPath().toString(), is("m/84'/1'/0'/0/21"));
            assertThat(address21.getAddress(), is("bcrt1q8knm24ect26h96563dwvtnjf07xmysr6v9cwyg"));
        }
    }
}
