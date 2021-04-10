package org.tbk.electrum;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.command.DaemonStatusResponse;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.Version;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ElectrumDaemonClientContainerTest {

    private static final String firstAddress = "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz";

    @SpringBootApplication
    public static class ElectrumDaemonContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired(required = false)
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Autowired(required = false)
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired(required = false)
    private ElectrumClient sut;

    @Test
    public void contextLoads() {
        assertThat(sut, is(notNullValue()));
        assertThat(electrumDaemonContainer, is(notNullValue()));
        assertThat(electrumDaemonContainer.isRunning(), is(true));

        assertThat(electrumxContainer, is(notNullValue()));
        assertThat(electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumDaemonContainer).blockFirst();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    public void testDaemonVersion() {
        Version version = sut.daemonVersion();

        assertThat(version.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    public void testDaemonStatus() {
        DaemonStatusResponse daemonStatusResponse = sut.daemonStatus();

        assertThat(daemonStatusResponse, is(notNullValue()));

        assertThat(daemonStatusResponse.isConnected(), is(true));
        assertThat(daemonStatusResponse.isAutoConnect(), is(true));
        assertThat(daemonStatusResponse.getVersion(), is(not(emptyOrNullString())));

        Map<String, Boolean> wallets = daemonStatusResponse.getWallets();
        assertThat(wallets.keySet(), hasSize(1));

        Map.Entry<String, Boolean> walletSyncState = wallets.entrySet().stream()
                .findFirst().orElseThrow();

        assertThat(walletSyncState.getKey(), is("/home/electrum/.electrum/regtest/wallets/default_wallet"));
        assertThat(walletSyncState.getValue(), is(true));
    }

    @Test
    public void testWalletSynchronized() {
        Boolean walletSynchronized = sut.isWalletSynchronized();

        assertThat(walletSynchronized, is(true));
    }

    @Test
    public void testOwnerOfAddress() {
        Boolean ownerOfAddress = sut.isOwnerOfAddress(firstAddress);
        assertThat(ownerOfAddress, is(true));

        // an address not controlled by wallet (taken from "second_wallet")
        String addressNotControlledByWallet = "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j";
        Boolean ownerOfAddress2 = sut.isOwnerOfAddress(addressNotControlledByWallet);
        assertThat(ownerOfAddress2, is(false));
    }

    @Test
    public void testGetPublicKeys() {
        List<String> publicKeys = this.sut.getPublicKeys(firstAddress);
        String firstPublicKey = publicKeys.stream()
                .findFirst().orElseThrow(IllegalStateException::new);

        assertThat(firstPublicKey, is("02595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c"));
    }

    @Test
    public void testListAddresses() {
        List<String> addresses = sut.listAddresses();

        assertThat(addresses, hasSize(greaterThan(0)));
        assertThat(addresses, hasItem(firstAddress));
    }

    @Test
    public void testGetBalance() {
        Balance balance = sut.getBalance();

        assertThat(balance, is(notNullValue()));
        assertThat(balance.getTotal(), is(notNullValue()));
        assertThat(balance.getUnconfirmed(), is(notNullValue()));
        assertThat(balance.getConfirmed(), is(notNullValue()));
    }

    @Test
    public void testSignAndVerifyMessage() {
        String address = firstAddress;
        String randomMessage = RandomStringUtils.randomAlphanumeric(127);

        String signedMessage = sut.signMessage(address, randomMessage, null);

        Boolean valid = sut.verifyMessage(address, signedMessage, randomMessage);
        assertThat(valid, is(true));

        Boolean valid2 = sut.verifyMessage(address, signedMessage, randomMessage + "1");
        assertThat(valid2, is(false));
    }

    @Test
    public void testEncryptAndDecryptMessage() {
        List<String> publicKeys = this.sut.getPublicKeys(firstAddress);
        String firstPublicKey = publicKeys.stream()
                .findFirst().orElseThrow(IllegalStateException::new);

        String message = RandomStringUtils.randomAlphanumeric(255);
        String encryptedMessage = this.sut.encryptMessage(firstPublicKey, message);

        assertThat(encryptedMessage, is(not(emptyOrNullString())));

        String decryptedMessage = this.sut.decryptMessage(firstPublicKey, encryptedMessage, null);
        assertThat(decryptedMessage, is(message));
    }
}

