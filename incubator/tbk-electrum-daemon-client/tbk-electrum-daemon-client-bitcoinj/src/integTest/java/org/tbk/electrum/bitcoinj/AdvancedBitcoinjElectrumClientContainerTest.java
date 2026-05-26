package org.tbk.electrum.bitcoinj;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.common.GetTransactionParams;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxo;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.IsSynchronizedParams;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdvancedBitcoinjElectrumClientContainerTest {

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
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Autowired
    private ElectrumRegtestActions electrumRegtestActions;

    @Autowired
    private WalletParams defaultWalletParams;

    @Autowired
    private BitcoinjElectrumClient sut;

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(sut, is(notNullValue()));
    }

    @Test
    void testWalletSynchronized() {
        // wallet might need some time to be synchronized as some addresses beyond
        // the gap limit are created in other test methods
        Boolean walletSynchronized = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.delegate().isWalletSynchronized(IsSynchronizedParams.builder()
                        .walletPath(defaultWalletParams.getWalletPath())
                        .build()))
                .filter(it -> it)
                .blockFirst(Duration.ofSeconds(10));

        assertThat("wallet is synchronized", walletSynchronized, is(true));
    }

    @Test
    void itShouldHaveFluentSyntaxToSendBalance() {
        Stopwatch sw = Stopwatch.createStarted();
        Address address1 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        Address address2 = sut.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        Coin balanceOnAddress2Before = this.sut.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2Before, is(Coin.ZERO));

        AtomicReference<Sha256Hash> firstSentTxHash = new AtomicReference<>();

        Coin amountSentFromAddress1ToAddress2 = Flux.from(bitcoinRegtestActions.mineBlock())
                .flatMap(lastBlockHash -> bitcoinRegtestActions.fundAddress(() -> address1))
                .flatMap(minedBlockHashes -> electrumRegtestActions.awaitExactPayment(defaultWalletParams, Coin.FIFTY_COINS, address1))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(defaultWalletParams, Coin.FIFTY_COINS, address1))
                .flatMap(balanceOnAddress -> electrumRegtestActions.awaitSpendableBalance(defaultWalletParams, Coin.FIFTY_COINS))
                // PAYMENT 1337 sats
                .flatMap(receivedAmount -> electrumRegtestActions.sendPayment(defaultWalletParams, address2, Coin.valueOf(1337)))
                .doOnNext(firstSentTxHash::set)
                .flatMap(txId -> electrumRegtestActions.awaitTransaction(defaultWalletParams, txId, 0))
                .flatMap(tx -> electrumRegtestActions.awaitExactPayment(defaultWalletParams, Coin.valueOf(1337), address2))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(defaultWalletParams, Coin.valueOf(1337), address2))
                .blockFirst(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        Coin balanceOnAddress2After = this.sut.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2After, is(Coin.valueOf(1337)));
        assertThat(amountSentFromAddress1ToAddress2, is(balanceOnAddress2After));

        BitcoinjUtxos addressUnspent = this.sut.getUtxosByAddress(address2);
        assertThat(addressUnspent.getValue(), is(balanceOnAddress2After));
        assertThat(addressUnspent.getUtxos(), hasSize(1));

        BitcoinjUtxo firstUtxo = addressUnspent.getUtxos().stream()
                .findFirst()
                .orElseThrow();

        assertThat(firstUtxo.getTxHash(), is(firstSentTxHash.get()));
        assertThat(firstUtxo.getValue(), is(Coin.valueOf(1337)));

        Flux.from(bitcoinRegtestActions.mineBlocks(21)).blockFirst(Duration.ofSeconds(90));
        Flux.from(electrumRegtestActions.awaitTransaction(defaultWalletParams, firstSentTxHash.get(), 21));

        Transaction transaction = this.sut.getTransaction(GetTransactionParams.builder()
                .txid(firstSentTxHash.get())
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        TransactionOutput output = transaction.getOutput(firstUtxo.getTxPos());
        assertThat(output.getValue(), is(Coin.valueOf(1337)));

        Address addressInUtxo = output.getScriptPubKey().getToAddress(RegTestParams.get());
        assertThat(addressInUtxo, is(address2));
    }
}
