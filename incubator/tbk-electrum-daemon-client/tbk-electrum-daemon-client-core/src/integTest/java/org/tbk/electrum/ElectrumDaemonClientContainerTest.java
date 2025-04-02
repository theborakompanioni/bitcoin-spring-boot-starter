package org.tbk.electrum;

import com.github.arteam.simplejsonrpc.client.exception.JsonRpcException;
import com.github.arteam.simplejsonrpc.core.domain.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.command.DaemonLoadWalletParams;
import org.tbk.electrum.command.GetInfoResponse;
import org.tbk.electrum.command.ListWalletEntry;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.Version;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumDaemonClientContainerTest {

    private static final String firstAddress = "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz";

    @SpringBootApplication(proxyBeanMethods = false)
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
    @Order(1)
    void contextLoads() {
        assertThat(sut, is(notNullValue()));
        assertThat(electrumDaemonContainer, is(notNullValue()));
        assertThat("electrum daemon container is running", electrumDaemonContainer.isRunning(), is(true));

        assertThat(electrumxContainer, is(notNullValue()));
        assertThat("electrumx container is running", electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumDaemonContainer).block();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void testDaemonVersion() {
        Version version = sut.daemonVersion();

        assertThat(version.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testGetInfo() {
        GetInfoResponse infoResponse = sut.getInfo();

        assertThat(infoResponse.getNetwork(), is("regtest"));
        assertThat(infoResponse.getPath(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getServer(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getBlockchainHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getServerHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getSpvNodes(), is(greaterThanOrEqualTo(0)));
        assertThat(infoResponse.isConnected(), is(true));
        assertThat(infoResponse.isAutoConnect(), is(true));
        assertThat(infoResponse.getVersion(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getFeePerKb(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testMakeSeed() {
        List<String> result = sut.createMnemonicSeed();

        assertThat(result, hasSize(12));
    }

    @Test
    void testGetSeed() {
        List<String> result = sut.getMnemonicSeed(null);

        assertThat(String.join(" ", result), is("truth fever mom transfer steak immense lake jacket glide bring fancy electric"));
    }

    @Test
    void testListWallets() {
        List<ListWalletEntry> wallets = sut.listOpenWallets();
        assertThat(wallets, hasSize(1));

        ListWalletEntry listWalletEntry = wallets.stream().findFirst().orElseThrow();

        assertThat("wallet is known", listWalletEntry.getPath(), is("/home/electrum/.electrum/regtest/wallets/default_wallet"));
        assertThat("wallet is synchronized", listWalletEntry.getSynced(), is(notNullValue()));
        assertThat("wallet is locked", listWalletEntry.getUnlocked(), is(false));
    }

    @Test
    void testLoadWallet() {
        boolean result = sut.loadWallet(DaemonLoadWalletParams.builder().build());

        assertThat(result, is(true));
    }

    @Test
    void testLoadWalletError() {
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.loadWallet(DaemonLoadWalletParams.builder()
                    .walletPath("/not/existing/wallet")
                    .build());
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getCode(), is(2));
        assertThat(error.getMessage(), is("internal error while executing RPC"));
    }

    @Test
    void testWalletSynchronized() {
        Boolean walletSynchronized = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.isWalletSynchronized())
                .filter(it -> it)
                .blockFirst(Duration.ofSeconds(10));

        assertThat("wallet is synchronized", walletSynchronized, is(true));
    }

    @Test
    void testOwnerOfAddress() {
        Boolean ownerOfAddress = sut.isOwnerOfAddress(firstAddress);
        assertThat("address is controlled by wallet", ownerOfAddress, is(true));

        // an address not controlled by wallet (taken from "second_wallet")
        String addressNotControlledByWallet = "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j";
        Boolean ownerOfAddress2 = sut.isOwnerOfAddress(addressNotControlledByWallet);
        assertThat("address is not controlled by wallet", ownerOfAddress2, is(false));
    }

    @Test
    void testGetPublicKeys() {
        List<String> publicKeys = this.sut.getPublicKeys(firstAddress);
        String firstPublicKey = publicKeys.stream()
                .findFirst().orElseThrow(IllegalStateException::new);

        assertThat(firstPublicKey, is("02595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c"));
    }

    @Test
    void testListAddresses() {
        List<String> addresses = sut.listAddresses();

        assertThat(addresses, hasSize(greaterThan(0)));
        assertThat(addresses, hasItem(firstAddress));
    }

    @Test
    void testGetBalance() {
        Balance balance = sut.getBalance();

        assertThat(balance, is(notNullValue()));
        assertThat(balance.getTotal(), is(notNullValue()));
        assertThat(balance.getUnconfirmed(), is(notNullValue()));
        assertThat(balance.getConfirmed(), is(notNullValue()));
        assertThat(balance.getSpendable(), is(notNullValue()));
        assertThat(balance.getUnmatured(), is(notNullValue()));
    }

    @Test
    void testSignAndVerifyMessage() {
        String address = firstAddress;
        String randomMessage = RandomStringUtils.randomAlphanumeric(127);

        String signedMessage = sut.signMessage(address, randomMessage, null);

        Boolean valid = sut.verifyMessage(address, signedMessage, randomMessage);
        assertThat(valid, is(true));

        Boolean valid2 = sut.verifyMessage(address, signedMessage, randomMessage + "1");
        assertThat(valid2, is(false));
    }

    @Test
    void testEncryptAndDecryptMessage() {
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

