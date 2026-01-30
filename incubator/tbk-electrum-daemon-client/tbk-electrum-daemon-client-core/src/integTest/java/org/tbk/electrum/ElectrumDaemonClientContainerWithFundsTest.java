package org.tbk.electrum;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.*;
import org.tbk.electrum.rpc.command.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumDaemonClientContainerWithFundsTest {
    // an address not controlled by wallet (taken from "second_wallet")
    private static final String addressNotControlledByWallet = "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j";

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumDaemonContainerWithFundsTestApplication {
        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerWithFundsTestApplication.class)
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
    private RegtestMiner regtestMiner;

    @Autowired
    private ElectrumRegtestFaucet electrumRegtestFaucet;

    @Autowired
    private ElectrumClient sut;

    @Autowired
    private WalletParams defaultWalletParams;

    @BeforeEach
    void tryLoadWallet() {
        try {
            log.trace("Load default wallet before test case...");
            sut.loadWallet(LoadWalletParams.builder()
                    .walletPath(defaultWalletParams.getWalletPath())
                    .build());
            log.trace("Successfully loaded default wallet before test case.");
        } catch (Exception e) {
            throw new IllegalStateException("Could not load default wallet.");
        }
    }

    @BeforeEach
    void waitForWalletSynchronization() throws Exception {
        sut.waitForWalletSynchronization(defaultWalletParams).get(30, TimeUnit.SECONDS);
    }

    @Test
    void testGetUtxos() throws Exception {
        Utxos utxosBefore = sut.getUtxos(ListUnspentParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        String address0 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        Coin requestAmount = Coin.valueOf(21_000);
        Sha256Hash txHash = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                requestAmount
        ).block(Duration.ofSeconds(90));
        assertThat(txHash, is(notNullValue()));

        sut.waitForWalletSynchronization(defaultWalletParams).get(30, TimeUnit.SECONDS);

        Utxos utxosAfter = sut.getUtxos(ListUnspentParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        assertThat(utxosAfter.getUtxos(), hasSize(utxosBefore.getUtxos().size() + 1));

        Utxo utxo = utxosAfter.getUtxos().stream()
                .filter(it -> txHash.equals(Sha256Hash.wrap(it.getTxHash())))
                .findFirst()
                .orElse(null);
        assertThat(utxo, is(notNullValue()));
        assertThat(utxo.getValue().getValue(), is(requestAmount.getValue()));
    }

    @Test
    void testPayto() throws Exception {
        String address0 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                Coin.valueOf(42_000)
        ).block(Duration.ofSeconds(90));

        sut.waitForWalletSynchronization(defaultWalletParams).get(30, TimeUnit.SECONDS);

        String destinationAddress = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        String changeAddress = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        Coin sendAmount = Coin.valueOf(2_100);
        Coin fee = Coin.valueOf(2_100);
        RawTx rawTx = sut.createTransaction(PaytoParams.builder()
                .destination(destinationAddress)
                .changeAddress(changeAddress)
                .amount(sendAmount.toBtc().toPlainString())
                .fee(fee.toBtc().toPlainString())
                .walletPath(defaultWalletParams.getWalletPath())
                .password(defaultWalletParams.getPassword().orElse(null))
                .unsigned(false)
                .addTransaction(true)
                .build());

        assertThat(rawTx, is(notNullValue()));
        assertThat(rawTx.getHex(), is(not(emptyOrNullString())));

        Tx deserializedTx = sut.getDeserializedTransaction(rawTx);
        assertThat(deserializedTx, is(notNullValue()));
        assertThat(deserializedTx.getOutputs(), hasSize(2));

        int heightBeforeBroadcast = sut.getInfo().getServerHeight();
        String txid = sut.broadcast(rawTx);
        assertThat(txid, is(not(emptyOrNullString())));

        // Check that the **unconfirmed** transaction is in the onchain history
        OnchainHistory historyWithUnconfirmedTransactions = sut.getOnchainHistory(OnchainHistoryParams.builder()
                // **ATTENTION**: toHeight/fromHeight cannot be used since tx is almost certainly unconfirmed
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        OnchainHistory.Transaction unconfirmedTx = historyWithUnconfirmedTransactions.getTransactions().stream()
                .filter(it -> txid.equalsIgnoreCase(it.getTxHash()))
                .findFirst()
                .orElse(null);
        assertThat("Transaction should be in onchain history", unconfirmedTx, is(notNullValue()));
        assertThat(unconfirmedTx.isIncoming(), is(false));
        assertThat(unconfirmedTx.getValue().getValue(), is(sendAmount.negate().getValue()));
        assertThat(unconfirmedTx.getOutputs(), hasSize(2));
        assertThat(unconfirmedTx.getConfirmations(), is(greaterThanOrEqualTo(0L)));

        OnchainHistory.HistoryTxOutput destinationTxout = unconfirmedTx.getOutputs().stream()
                .filter(it -> destinationAddress.equals(it.getAddress().orElse(null)))
                .findFirst()
                .orElseThrow();
        assertThat(destinationTxout, is(notNullValue()));
        assertThat(destinationTxout.getValue().getValue(), is(sendAmount.getValue()));

        OnchainHistory.HistoryTxOutput changeTxout = unconfirmedTx.getOutputs().stream()
                .filter(it -> changeAddress.equals(it.getAddress().orElse(null)))
                .findFirst()
                .orElse(null);
        assertThat(changeTxout, is(notNullValue()));

        sut.waitForWalletSynchronization(defaultWalletParams).get(30, TimeUnit.SECONDS);

        int heightAfterBroadcast = sut.getInfo().getServerHeight();
        regtestMiner.mineBlocks(1);
        waitForBlockHeightIncrease(sut, heightAfterBroadcast).block(Duration.ofSeconds(30));

        // tx might not have been mined the first time - mine a second block to be sure
        int heightAfterBroadcast2 = sut.getInfo().getServerHeight();
        regtestMiner.mineBlocks(1);
        waitForBlockHeightIncrease(sut, heightAfterBroadcast2).block(Duration.ofSeconds(30));

        sut.waitForWalletSynchronization(defaultWalletParams).get(30, TimeUnit.SECONDS);

        // Check that the **confirmed** transaction is in the onchain history
        OnchainHistory historyWithoutUnconfirmedTransactions = sut.getOnchainHistory(OnchainHistoryParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                // fromHeight/toHeight can be used since tx is confirmed
                .fromHeight(heightBeforeBroadcast)
                .toHeight(Integer.MAX_VALUE)
                .build());
        OnchainHistory.Transaction confirmedTx = historyWithoutUnconfirmedTransactions.getTransactions().stream()
                .filter(it -> unconfirmedTx.getTxHash().equals(it.getTxHash()))
                .findFirst()
                .orElse(null);
        assertThat("Transaction should be in onchain history", confirmedTx, is(notNullValue()));
        assertThat("tx has is the same", confirmedTx.getTxHash(), is(unconfirmedTx.getTxHash()));
        assertThat(confirmedTx.isIncoming(), is(unconfirmedTx.isIncoming()));
        assertThat(confirmedTx.getValue().getValue(), is(unconfirmedTx.getValue().getValue()));
        assertThat(confirmedTx.getOutputs(), hasSize(unconfirmedTx.getOutputs().size()));
        assertThat(confirmedTx.getConfirmations(), is(greaterThanOrEqualTo(1L)));
        assertThat(confirmedTx.getHeight().isPresent(), is(true));
        assertThat(confirmedTx.getTxPosInBlock().isPresent(), is(true));
        assertThat(confirmedTx.getTimestamp().isPresent(), is(true));
    }

    @Test
    void testGetAddressHistory() {
        String address0 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        Sha256Hash txHash = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                Coin.valueOf(21_000)
        ).block(Duration.ofSeconds(90));
        assertThat(txHash, is(notNullValue()));

        List<TxHashAndBlockHeight> addressHistory = sut.getAddressHistory(address0);
        assertThat(addressHistory, hasSize(1));

        Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> {
            TxHashAndBlockHeight txHashAndBlockHeight = addressHistory.stream()
                    .filter(it -> it.getTxHash().equals(txHash.toString()))
                    .findFirst()
                    .orElse(null);
            return txHashAndBlockHeight.getHeight();
        }, is(greaterThanOrEqualTo(0L)));
    }

    @Test
    void testDeserializeTransaction() {
        String address0 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        Coin requestAmount = Coin.valueOf(21_000);
        Sha256Hash txHash = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                requestAmount
        ).block(Duration.ofSeconds(90));
        assertThat(txHash, is(notNullValue()));

        RawTx rawTx = sut.getRawTransaction(GetTransactionParams.builder()
                .txid(txHash.toString())
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        assertThat(rawTx, is(notNullValue()));
        assertThat(rawTx.getHex(), is(not(emptyOrNullString())));

        Tx deserializedTx = sut.getDeserializedTransaction(rawTx);
        assertThat(deserializedTx, is(notNullValue()));

        Tx.TxOutput txOutput = deserializedTx.getOutputs().stream()
                .filter(it -> address0.equals(it.getAddress().orElse(null)))
                .findFirst()
                .orElse(null);
        assertThat(txOutput, is(notNullValue()));
        assertThat(txOutput.getValue().getValue(), is(requestAmount.getValue()));
    }

    @Test
    void testDeserializeTransactionFromTxOfOtherWallet() {
        Coin requestAmount = Coin.valueOf(21_000);
        Sha256Hash txHash = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), addressNotControlledByWallet),
                requestAmount
        ).block(Duration.ofSeconds(90));
        assertThat(txHash, is(notNullValue()));

        RawTx rawTx = sut.getRawTransaction(GetTransactionParams.builder()
                .txid(txHash.toString())
                .build());
        Tx deserializedTx = sut.getDeserializedTransaction(rawTx);
        assertThat(deserializedTx, is(notNullValue()));

        Tx.TxOutput txOutput = deserializedTx.getOutputs().stream()
                .filter(it -> addressNotControlledByWallet.equals(it.getAddress().orElse(null)))
                .findFirst()
                .orElse(null);
        assertThat(txOutput, is(notNullValue()));
        assertThat(txOutput.getValue().getValue(), is(requestAmount.getValue()));
    }

    @Test
    void testDeserializeTransactionFromCoinbase() {
        Balance balanceBefore = sut.getBalance(GetBalanceParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        String address0 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        List<Sha256Hash> blocks = regtestMiner.mineBlocks(1, () -> Address.fromString(RegTestParams.get(), address0));
        assertThat(blocks, hasSize(1));

        Balance balanceAfter = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.waitForWalletSynchronization(defaultWalletParams))
                .map(it -> sut.getBalance(GetBalanceParams.builder()
                        .walletPath(defaultWalletParams.getWalletPath())
                        .build()))
                .filter(it -> it.getTotal().getValue() > balanceBefore.getTotal().getValue())
                .blockFirst(Duration.ofSeconds(60));
        assertThat(balanceAfter, is(notNullValue()));
        assertThat(balanceAfter.getUnmatured().getValue(), is(greaterThan(balanceBefore.getUnmatured().getValue())));

        List<TxHashAndBlockHeight> addressHistory = sut.getAddressHistory(address0);
        assertThat(addressHistory, hasSize(1));

        RawTx rawTx = sut.getRawTransaction(GetTransactionParams.builder()
                .txid(addressHistory.getFirst().getTxHash())
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        Tx deserializedTx = sut.getDeserializedTransaction(rawTx);
        assertThat(deserializedTx, is(notNullValue()));
        assertThat(deserializedTx.getInputs(), hasSize(1));

        Tx.TxInput txInput = deserializedTx.getInputs().getFirst();
        assertThat(txInput.getAddress().isPresent(), is(false));

        Tx.TxOutput txOutput = deserializedTx.getOutputs().stream()
                .filter(it -> address0.equals(it.getAddress().orElse(null)))
                .findFirst()
                .orElse(null);
        assertThat(txOutput, is(notNullValue()));
        assertThat(txOutput.getValue().getValue(), is(greaterThanOrEqualTo(0L)));
    }

    private static Mono<Integer> waitForBlockHeightIncrease(ElectrumClient client) {
        return Mono.defer(() -> {
            int currentServerHeight = client.getInfo().getServerHeight();
            return waitForBlockHeightIncrease(client, currentServerHeight);
        });
    }

    private static Mono<Integer> waitForBlockHeightIncrease(ElectrumClient client, int refHeight) {
        return Flux.interval(Duration.ofMillis(100))
                .doOnNext(it -> log.trace("Waiting for wallet to receive new blocks.. ({} attempt)", it))
                .map(it -> client.getInfo().getBlockchainHeight())
                .filter(newBlockchainHeight -> newBlockchainHeight > refHeight)
                .next();
    }
}
