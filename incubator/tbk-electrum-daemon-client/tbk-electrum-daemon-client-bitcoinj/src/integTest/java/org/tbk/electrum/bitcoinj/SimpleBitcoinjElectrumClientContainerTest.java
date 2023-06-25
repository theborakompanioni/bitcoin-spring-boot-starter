package org.tbk.electrum.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.command.DaemonStatusResponse;
import org.tbk.electrum.model.Version;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class SimpleBitcoinjElectrumClientContainerTest {

    private static final Address firstAddress = Address.fromString(RegTestParams.get(), "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz");

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumDaemonContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired
    private BitcoinjElectrumClient sut;

    @Test
    void testDaemonVersion() {
        Version version = sut.delegate().daemonVersion();

        assertThat(version.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testDaemonStatus() {
        DaemonStatusResponse daemonStatusResponse = sut.delegate().daemonStatus();

        assertThat(daemonStatusResponse, is(notNullValue()));

        assertThat(daemonStatusResponse.isConnected(), is(true));
        assertThat(daemonStatusResponse.isAutoConnect(), is(true));
        assertThat(daemonStatusResponse.getVersion(), is(not(emptyOrNullString())));

        Map<String, Boolean> wallets = daemonStatusResponse.getWallets();
        assertThat(wallets.keySet(), hasSize(1));

        Map.Entry<String, Boolean> walletSyncState = wallets.entrySet().stream()
                .findFirst().orElseThrow();

        assertThat("wallet is known", walletSyncState.getKey(), is("/home/electrum/.electrum/regtest/wallets/default_wallet"));

        if (walletSyncState.getValue() == Boolean.FALSE) {
            // might need some time to be loaded
            Boolean walletIsLoaded = Flux.interval(Duration.ofMillis(100))
                    .map(it -> sut.delegate().daemonStatus())
                    .map(DaemonStatusResponse::getWallets)
                    .map(it -> it.entrySet().stream().findFirst().orElseThrow())
                    .map(Map.Entry::getValue)
                    .filter(it -> it)
                    .blockFirst(Duration.ofSeconds(3));

            assertThat("wallet is loaded", walletIsLoaded, is(true));
        }
    }

    @Test
    void testWalletSynchronized() {
        // wallet might need some time to be synchronized as some addresses beyond the gap limit are created in other methods
        Boolean walletSynchronized = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.delegate().isWalletSynchronized())
                .filter(it -> it)
                .blockFirst(Duration.ofSeconds(3));

        assertThat("wallet is synchronized", walletSynchronized, is(true));
    }

    @Test
    void testOwnerOfAddress() {
        Boolean ownerOfAddress = sut.isOwnerOfAddress(firstAddress);
        assertThat("address is controlled by wallet", ownerOfAddress, is(true));

        // an address not controlled by wallet (taken from "second_wallet")
        Address addressNotControlledByWallet = Address.fromString(RegTestParams.get(), "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j");
        Boolean ownerOfAddress2 = sut.isOwnerOfAddress(addressNotControlledByWallet);
        assertThat("address is not controlled by wallet", ownerOfAddress2, is(false));
    }

    @Test
    void testListAddresses() {
        List<Address> addresses = sut.listAddresses();

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
        Address newAddress = sut.createNewAddress();

        assertThat(newAddress, is(notNullValue()));
        assertThat(newAddress.getOutputScriptType(), is(Script.ScriptType.P2WPKH));
    }

    @Test
    void testGetAddressBalance() {
        Address newAddress = sut.createNewAddress();
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
        BitcoinjBalance balance = sut.getBalance();

        assertThat(balance, is(notNullValue()));
        assertThat(balance.getTotal(), is(notNullValue()));
        assertThat(balance.getUnconfirmed(), is(notNullValue()));
        assertThat(balance.getConfirmed(), is(notNullValue()));
        assertThat(balance.getSpendable(), is(notNullValue()));
        assertThat(balance.getUnmatured(), is(notNullValue()));
    }

    @Test
    void testGetAddressUnspent() {
        Address newAddress = sut.createNewAddress();
        BitcoinjUtxos addressUnspent = sut.getAddressUnspent(newAddress);

        assertThat(addressUnspent, is(notNullValue()));
        assertThat(addressUnspent.getValue(), is(Coin.ZERO));
        assertThat(addressUnspent.getUtxos(), hasSize(0));
    }
}

