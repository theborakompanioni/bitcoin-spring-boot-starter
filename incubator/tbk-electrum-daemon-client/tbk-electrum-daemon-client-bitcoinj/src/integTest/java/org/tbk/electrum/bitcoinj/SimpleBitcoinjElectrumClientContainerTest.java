package org.tbk.electrum.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.bitcoinj.common.GetPubkeysParams;
import org.tbk.electrum.bitcoinj.common.IsMineParams;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.Version;
import org.tbk.electrum.rpc.command.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class SimpleBitcoinjElectrumClientContainerTest {

    private static final Address firstAddress = Address.fromString(RegTestParams.get(), "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz");

    // an address not controlled by wallet (taken from "second_wallet")
    private static final Address addressNotControlledByWallet = Address.fromString(RegTestParams.get(), "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j");

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumDaemonContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        @Primary
        WalletParams defaultWalletParams() {
            return WalletParams.builder()
                    .walletPath("/home/electrum/.electrum/regtest/wallets/default_wallet")
                    .build();
        }
    }

    @Autowired
    private BitcoinjElectrumClient sut;

    @Autowired
    private WalletParams defaultWalletParams;

    @Test
    void testDaemonVersion() {
        Version version = sut.delegate().daemonVersion();

        assertThat(version.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testGetInfo() {
        GetInfoResponse infoResponse = sut.delegate().getInfo();

        assertThat(infoResponse.getNetwork(), is("regtest"));
        assertThat(infoResponse.getPath(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getServer(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getBlockchainHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getServerHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getSpvNodes(), is(greaterThanOrEqualTo(0)));
        assertThat(infoResponse.isConnected(), is(true));
        assertThat(infoResponse.isAutoConnect(), is(false)); // auto connect is disabled when connecting to single server
        assertThat(infoResponse.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testListOpenWallets() {
        List<ListWalletEntry> wallets = sut.delegate().listOpenWallets();

        if (wallets.isEmpty()) {
            Boolean success = sut.delegate().loadWallet(LoadWalletParams.builder()
                    .walletPath(defaultWalletParams.getWalletPath())
                    .password(defaultWalletParams.getPassword().orElse(null))
                    .build());
            assertThat(success, is(true));

            wallets = sut.delegate().listOpenWallets();
        }

        ListWalletEntry listWalletEntry = wallets.stream()
                .filter(it -> it.getPath().equals(defaultWalletParams.getWalletPath()))
                .findFirst().orElseThrow();

        assertThat(listWalletEntry, is(notNullValue()));
        assertThat("wallet is synchronized", listWalletEntry.getSynced(), is(notNullValue()));
        assertThat("wallet is locked", listWalletEntry.getUnlocked(), is(true));
    }

    @Test
    void testWalletSynchronized() {
        // wallet might need some time to be synchronized as some addresses beyond the gap limit are created in other methods
        Boolean walletSynchronized = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.delegate().isWalletSynchronized(IsSynchronizedParams.builder()
                        .walletPath(defaultWalletParams.getWalletPath())
                        .build()))
                .filter(it -> it)
                .blockFirst(Duration.ofSeconds(10));

        assertThat("wallet is synchronized", walletSynchronized, is(true));
    }

    @Test
    void testOwnerOfAddress() {
        Boolean ownerOfAddress = sut.isOwnerOfAddress(IsMineParams.builder()
                .address(firstAddress)
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        assertThat("address is controlled by wallet", ownerOfAddress, is(true));

        Boolean ownerOfAddress2 = sut.isOwnerOfAddress(IsMineParams.builder()
                .address(addressNotControlledByWallet)
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        assertThat("address is not controlled by wallet", ownerOfAddress2, is(false));
    }

    @Test
    void testGetPublicKeys() {
        List<ECKey> publicKeys = sut.getPublicKeys(GetPubkeysParams.builder()
                .address(firstAddress)
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        ECKey firstPublicKey = publicKeys.stream().findFirst()
                .orElseThrow(IllegalStateException::new);

        assertThat(firstPublicKey.getPublicKeyAsHex(), is("02595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c"));
        assertThat(firstPublicKey.isPubKeyOnly(), is(true));
        assertThat(firstPublicKey.isCompressed(), is(true));
        assertThat(firstPublicKey.isEncrypted(), is(false));
    }

    @Test
    void testListAddresses() {
        List<Address> addresses = sut.listAddresses(ListAddressParams.all(defaultWalletParams.getWalletPath()));

        assertThat(addresses, hasSize(greaterThan(0)));
        assertThat(addresses, hasItem(firstAddress));
    }

    @Test
    void testGetUnusedAddress() {
        Address unusedAddress = sut.getUnusedAddress().orElseThrow();

        assertThat(unusedAddress, is(notNullValue()));
        assertThat(unusedAddress.getOutputScriptType(), is(Script.ScriptType.P2WPKH));
    }

    @Test
    void testCreateNewAddress() {
        Address newAddress = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        assertThat(newAddress, is(notNullValue()));
        assertThat(newAddress.getOutputScriptType(), is(Script.ScriptType.P2WPKH));
    }

    @Test
    void testGetAddressBalance() {
        Address newAddress = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        BitcoinjBalance addressBalance = sut.getAddressBalance(newAddress);

        assertThat(addressBalance, is(notNullValue()));
        assertThat(addressBalance.getTotal(), is(Coin.ZERO));
        assertThat(addressBalance.getUnconfirmed(), is(Coin.ZERO));
        assertThat(addressBalance.getConfirmed(), is(Coin.ZERO));
        assertThat(addressBalance.getSpendable(), is(Coin.ZERO));
        assertThat(addressBalance.getUnmatured(), is(Coin.ZERO));
    }

    @Test
    void testGetBalance() {
        BitcoinjBalance balance = sut.getBalance(GetBalanceParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        assertThat(balance, is(notNullValue()));
        assertThat(balance.getTotal(), is(notNullValue()));
        assertThat(balance.getUnconfirmed(), is(notNullValue()));
        assertThat(balance.getConfirmed(), is(notNullValue()));
        assertThat(balance.getSpendable(), is(notNullValue()));
        assertThat(balance.getUnmatured(), is(notNullValue()));
    }

    @Test
    void testGetUtxosByAddress() {
        Address newAddress = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        BitcoinjUtxos addressUnspent = sut.getUtxosByAddress(newAddress);

        assertThat(addressUnspent, is(notNullValue()));
        assertThat(addressUnspent.getValue(), is(Coin.ZERO));
        assertThat(addressUnspent.getUtxos(), hasSize(0));
    }
}

