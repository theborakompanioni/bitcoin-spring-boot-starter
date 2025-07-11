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
import org.tbk.electrum.model.*;
import org.tbk.electrum.rpc.command.*;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumDaemonClientContainerTest {

    private static final String firstAddress = "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz";

    // an address not controlled by wallet (taken from "second_wallet")
    private static final String addressNotControlledByWallet = "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j";

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

    @BeforeEach
    void tryLoadWallet() {
        try {
            log.trace("Load default wallet before test case");
            sut.loadWallet(LoadWalletParams.builder().build());
        } catch (Exception e) {
            log.warn("Could not load default wallet");
        }
    }

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
    void testGetInfo() {
        GetInfoResponse infoResponse = sut.getInfo();

        assertThat(infoResponse.getNetwork(), is("regtest"));
        assertThat(infoResponse.getPath(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getServer(), is(not(emptyOrNullString())));
        assertThat(infoResponse.getBlockchainHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getServerHeight(), is(greaterThanOrEqualTo(-1)));
        assertThat(infoResponse.getSpvNodes(), is(greaterThanOrEqualTo(0)));
        assertThat(infoResponse.isConnected(), is(true));
        assertThat(infoResponse.isAutoConnect(), is(false));
        assertThat(infoResponse.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testGetFeerate() {
        Feerate feerate = sut.getFeerate();

        assertThat(feerate.getPolicy(), is(startsWith("eta:")));
        assertThat(feerate.getSatPerVbyte().getSatPerVbyte().longValue(), is(greaterThan(0L)));
    }

    @Test
    void testGetConfig() {
        Optional<Object> raw = sut.getConfig(ConfigKeyEnum.log_to_file);
        assertThat(raw.isPresent(), is(true));

        boolean value = raw.map(it -> Boolean.parseBoolean(it.toString())).orElse(false);
        assertThat(value, is(true));
    }

    @Test
    void testGetConfigError() {
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            Optional<Object> ignoredOnPurpose = sut.getConfig("non_existing_config_key");
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getMessage(), is("internal error while executing RPC"));
        assertThat(error.getData().get("exception").asText(), is("KeyError(\"No ConfigVar with key='non_existing_config_key'\")"));
        assertThat(error.getCode(), is(2));
    }

    @Test
    void testSetConfig() {
        boolean value0 = sut.getConfig(ConfigKeyEnum.network_offline)
                .map(it -> Boolean.parseBoolean(it.toString()))
                .orElse(true);
        assertThat(value0, is(false));

        sut.setConfig(ConfigKeyEnum.network_offline, String.valueOf(!value0));

        boolean value1 = sut.getConfig(ConfigKeyEnum.network_offline)
                .map(it -> Boolean.parseBoolean(it.toString()))
                .orElse(false);
        assertThat(value1, is(true));

        sut.setConfig(ConfigKeyEnum.network_offline, String.valueOf(!value1));
    }

    @Test
    void testChangeGapLimit() {
        boolean success = sut.changeGapLimit(ChangeGapLimitParams.builder()
                .gaplimit(10)
                .build());
        assertThat(success, is(true));
    }

    @Test
    void testGetMinAcceptableGap() {
        // needs a synchronized wallet
        sut.waitForWalletSynchronization();

        int value = sut.getMinAcceptableGap();
        assertThat(value, is(greaterThanOrEqualTo(1)));
    }

    @Test
    void testMakeSeed() {
        List<String> result = sut.createMnemonicSeed();

        assertThat(result, hasSize(12));
    }

    @Test
    void testCreateWalletSuccess() {
        Wallet wallet = sut.createWallet(CreateParams.builder()
                .walletPath("new_wallet")
                .build());

        assertThat(wallet.getFilePath(), is("/home/electrum/.electrum/regtest/wallets/new_wallet"));
        assertThat(wallet.getSeed().getWords(), hasSize(12));
    }

    @Test
    void testCreateWalletAndGetSeedSuccess() {
        Wallet wallet = sut.createWallet(CreateParams.builder()
                .walletPath("new_wallet_and_get_seed")
                .build());

        assertThat(wallet.getFilePath(), is("/home/electrum/.electrum/regtest/wallets/new_wallet_and_get_seed"));
        assertThat(wallet.getSeed().getWords(), hasSize(12));

        try {
            boolean loaded = sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(wallet.getFilePath())
                    .build());
            assertThat(loaded, is(true));

            List<String> result = sut.getMnemonicSeed(GetSeedParams.builder()
                    .walletPath(wallet.getFilePath())
                    .build());
            assertThat(String.join("", result), is(String.join("", wallet.getSeed().getWords())));
        } finally {
            Boolean closed = sut.closeWallet(CloseWalletParams.builder()
                    .walletPath(wallet.getFilePath())
                    .build());
            assertThat(closed, is(true));
        }
    }

    @Test
    void testCreateAndLoadEncryptedWalletSuccess() {
        Wallet wallet = sut.createWallet(CreateParams.builder()
                .walletPath("new_wallet_encrypted")
                .encryptFile(true)
                .password("correcthorsebatterystaple")
                .build());

        assertThat(wallet.getFilePath(), is("/home/electrum/.electrum/regtest/wallets/new_wallet_encrypted"));
        assertThat(wallet.getSeed().getWords(), hasSize(12));

        try {
            boolean loaded = sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(wallet.getFilePath())
                    .password("correcthorsebatterystaple")
                    .build());
            assertThat(loaded, is(true));
        } finally {
            Boolean closed = sut.closeWallet(CloseWalletParams.builder()
                    .walletPath(wallet.getFilePath())
                    .build());
            assertThat(closed, is(true));
        }
    }

    @Test
    void testCreateAndLoadEncryptedWalletError() {
        Wallet wallet = sut.createWallet(CreateParams.builder()
                .walletPath("new_wallet_encrypted_and_load")
                .encryptFile(true)
                .password("correcthorsebatterystaple")
                .build());

        assertThat(wallet.getFilePath(), is("/home/electrum/.electrum/regtest/wallets/new_wallet_encrypted_and_load"));

        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(wallet.getFilePath())
                    .password("wrong_password")
                    .build());
        });

        // error might be provided more dev friendly in upcoming releases, so this test case fail with future versions
        // but better this than nothing to check against
        assertThat(e.getErrorMessage().getMessage(), is("internal error while executing RPC"));
        assertThat(e.getErrorMessage().getData().get("exception").asText(), is("InvalidPassword()"));
    }

    @Test
    void testCreateWalletError() {
        Wallet ignoreOnPurpose = sut.createWallet(CreateParams.builder()
                .walletPath("new_wallet_error")
                .build());

        assertThat(ignoreOnPurpose.getFilePath(), is("/home/electrum/.electrum/regtest/wallets/new_wallet_error"));

        // try creating the wallet again should throw an error
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.createWallet(CreateParams.builder()
                    .walletPath("new_wallet_error")
                    .build());
        });

        assertThat(e.getErrorMessage().getMessage(), is("Remove the existing wallet first!"));
        assertThat(e.getErrorMessage().getCode(), is(1));
    }

    @Test
    void testRestoreWalletSuccess() {
        RestoreResponse result = sut.restoreWallet(RestoreParams.builder()
                .text("truth fever mom transfer steak immense lake jacket glide bring fancy electric")
                .walletPath("restored_wallet")
                .build());

        assertThat(result.getMessage(), is("This wallet was restored offline. It may contain more addresses than displayed. Start a daemon and use load_wallet to sync its history."));
        assertThat(result.getPath(), is("/home/electrum/.electrum/regtest/wallets/restored_wallet"));

        try {
            boolean loaded = sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(result.getPath())
                    .build());
            assertThat(loaded, is(true));
        } finally {
            Boolean closed = sut.closeWallet(CloseWalletParams.builder()
                    .walletPath(result.getPath())
                    .build());
            assertThat(closed, is(true));
        }
    }

    @Test
    void testRestoreAndLoadEncryptedWalletSuccess() {
        RestoreResponse result = sut.restoreWallet(RestoreParams.builder()
                .text("truth fever mom transfer steak immense lake jacket glide bring fancy electric")
                .walletPath("restored_wallet_encrypted")
                .encryptFile(true)
                .password("correcthorsebatterystaple")
                .build());

        assertThat(result.getMessage(), is("This wallet was restored offline. It may contain more addresses than displayed. Start a daemon and use load_wallet to sync its history."));
        assertThat(result.getPath(), is("/home/electrum/.electrum/regtest/wallets/restored_wallet_encrypted"));

        try {
            boolean loaded = sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(result.getPath())
                    .password("correcthorsebatterystaple")
                    .build());
            assertThat(loaded, is(true));
        } finally {
            Boolean closed = sut.closeWallet(CloseWalletParams.builder()
                    .walletPath(result.getPath())
                    .build());
            assertThat(closed, is(true));
        }
    }

    @Test
    void testGetSeed() {
        List<String> result = sut.getMnemonicSeed(GetSeedParams.builder().build());

        assertThat(String.join(" ", result), is("truth fever mom transfer steak immense lake jacket glide bring fancy electric"));
    }

    @Test
    void testListWallets() {
        List<ListWalletEntry> wallets = sut.listOpenWallets();
        assertThat(wallets, hasSize(greaterThanOrEqualTo(1)));

        ListWalletEntry listWalletEntry = wallets.stream().findFirst().orElseThrow();

        assertThat("wallet is known", listWalletEntry.getPath(), is("/home/electrum/.electrum/regtest/wallets/default_wallet"));
        assertThat("wallet is synchronized", listWalletEntry.getSynced(), either(is(true)).or(is(false)));
        assertThat("wallet is locked", listWalletEntry.getUnlocked(), is(true));
    }

    @Test
    void testLoadWallet() {
        boolean result = sut.loadWallet(LoadWalletParams.builder().build());

        assertThat(result, is(true));
    }

    @Test
    void testLoadWalletError() {
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.loadWallet(LoadWalletParams.builder()
                    .walletPath("/non/existing/wallet")
                    .build());
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getMessage(), is("internal error while executing RPC"));
        assertThat(error.getCode(), is(2));
    }

    @Test
    void testCloseWallet() {
        boolean result = sut.closeWallet(CloseWalletParams.builder().build());

        assertThat(result, is(true));
    }

    @Test
    void testCloseWalletError() {
        Boolean success = sut.closeWallet(CloseWalletParams.builder()
                .walletPath("/non/existing/wallet")
                .build());

        assertThat(success, is(false));
    }

    @Test
    void testOnchainHistory() {
        OnchainHistory history = sut.getOnchainHistory();

        assertThat(history.getTransactions(), is(hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void testOnchainCapitalGains() {
        OnchainSummary summary = sut.getOnchainCapitalGains();

        assertThat(summary.getStartBalance().isZero(), is(true));
        assertThat(summary.getEndBalance().isZero(), is(true));
        assertThat(summary.getIncoming().isZero(), is(true));
        assertThat(summary.getOutgoing().isZero(), is(true));
    }

    @Test
    void testWalletSynchronized() throws ExecutionException, InterruptedException, TimeoutException {
        Boolean walletSynchronized0 = sut.isWalletSynchronized();
        assertThat(walletSynchronized0, either(is(true)).or(is(false)));

        sut.waitForWalletSynchronization().get(10, TimeUnit.SECONDS);

        Boolean walletSynchronized1 = sut.isWalletSynchronized();
        assertThat("wallet is synchronized", walletSynchronized1, is(true));
    }

    @Test
    void testWalletSynchronizedError() {
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.isWalletSynchronized(IsSynchronizedParams.builder()
                    .walletPath("/non/existing/wallet")
                    .build());
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getMessage(), is("wallet not loaded"));
        assertThat(error.getCode(), is(1));
    }

    @Test
    void testOwnerOfAddress() {
        Boolean ownerOfAddress = sut.isOwnerOfAddress(firstAddress);
        assertThat("address is controlled by wallet", ownerOfAddress, is(true));

        Boolean ownerOfAddress2 = sut.isOwnerOfAddress(addressNotControlledByWallet);
        assertThat("address is not controlled by wallet", ownerOfAddress2, is(false));
    }

    @Test
    void testGetUnusedAddress() {
        Optional<String> unusedAddressOrEmpty = this.sut.getUnusedAddress();
        String unusedAddress = unusedAddressOrEmpty.orElseThrow();

        assertThat(unusedAddress, startsWith("bcrt1"));
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
    void testListAddressesWithBalance() {
        List<AddressWithBalance> addresses = sut.listAddressesWithBalance();

        assertThat(addresses, hasSize(greaterThan(0)));

        AddressWithBalance addressWithBalance = addresses.stream()
                .filter(it -> firstAddress.equals(it.getAddress()))
                .findFirst()
                .orElseThrow();
        assertThat(addressWithBalance.getBalance(), is(SimpleTxoValue.zero()));
    }

    @Test
    void testSetLabel() {
        List<AddressWithBalance> addressesWithLabels0 = sut.listAddressesWithBalance().stream()
                .filter(it -> it.getLabel().isPresent())
                .toList();
        assertThat(addressesWithLabels0, hasSize(0));

        String label = "label0";
        sut.setLabel(SetLabelParams.builder()
                .key(firstAddress)
                .label(label)
                .build());

        List<AddressWithBalance> addressesWithLabels1 = sut.listAddressesWithBalance().stream()
                .filter(it -> it.getLabel().isPresent())
                .toList();
        assertThat(addressesWithLabels1, hasSize(1));

        AddressWithBalance first = addressesWithLabels1.getFirst();
        assertThat(first.getAddress(), is(firstAddress));
        assertThat(first.getLabel().orElseThrow(), is(label));
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
    void testGetBalanceError() {
        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            sut.getBalance(GetBalanceParams.builder()
                    .walletPath("/non/existing/wallet")
                    .build());
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getMessage(), is("wallet not loaded"));
        assertThat(error.getCode(), is(1));
    }

    @Test
    void testGetBalanceErrorWalletNotLoaded() {
        Boolean closed = sut.closeWallet(CloseWalletParams.builder().build());
        assertThat(closed, is(true));

        JsonRpcException e = Assertions.assertThrows(JsonRpcException.class, () -> {
            Balance ignoredOnPurpose = sut.getBalance();
        });

        ErrorMessage error = e.getErrorMessage();
        assertThat(error.getMessage(), is("wallet not loaded"));
        assertThat(error.getCode(), is(1));
    }

    @Test
    void testSignAndVerifyMessage() {
        String address = firstAddress;
        String randomMessage = RandomStringUtils.randomAlphanumeric(127);

        String signedMessage = sut.signMessage(address, randomMessage, null);

        Boolean valid = sut.verifyMessage(address, signedMessage, randomMessage);
        assertThat(valid, is(true));

        Boolean valid2 = sut.verifyMessage(address, signedMessage, "21" + randomMessage);
        assertThat(valid2, is(false));

        Boolean valid3 = sut.verifyMessage(sut.createNewAddress(), signedMessage, randomMessage);
        assertThat(valid3, is(false));

        Boolean valid4 = sut.verifyMessage(addressNotControlledByWallet, signedMessage, randomMessage);
        assertThat(valid4, is(false));
    }

    @Test
    void testSignAndVerifyMessageWithWhitespaces() {
        String address = sut.createNewAddress();
        String message = "A message with whitespaces.";

        String signedMessage = sut.signMessage(address, message, null);

        Boolean valid = sut.verifyMessage(address, signedMessage, message);
        assertThat(valid, is(true));
    }

    @Test
    void testEncryptAndDecryptMessage() {
        List<String> publicKeys = this.sut.getPublicKeys(firstAddress);
        String firstPublicKey = publicKeys.stream()
                .findFirst().orElseThrow(IllegalStateException::new);

        String message = RandomStringUtils.randomAlphanumeric(255);
        String encryptedMessage = this.sut.encryptMessage(firstPublicKey, message);

        assertThat(encryptedMessage, is(not(emptyOrNullString())));

        String decryptedMessage = this.sut.decryptMessage(DecryptParams.builder()
                .publicKey(firstPublicKey)
                .encryptedMessage(encryptedMessage)
                .build());
        assertThat(decryptedMessage, is(message));
    }

    @Test
    void testDaemonVersion() {
        Version version = sut.daemonVersion();

        assertThat(version.getVersion(), is(not(emptyOrNullString())));
    }

    @Test
    void testDaemonVersionInfo() {
        Map<String, String> versionInfo = sut.daemonVersionInfo();

        assertThat(versionInfo, is(notNullValue()));
    }

    @Test
    void testValidateAddress() {
        assertThat(sut.isValidAddress(firstAddress), is(true));
        assertThat(sut.isValidAddress(addressNotControlledByWallet), is(true));
        assertThat(sut.isValidAddress("invalid_address"), is(false));
    }
}
