package org.tbk.electrum.bitcoinj;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxo;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class AdvancedBitcoinjElectrumClientContainerTest {

    private static final Address firstAddress = Address.fromString(RegTestParams.get(), "bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz");

    @SpringBootApplication
    public static class ElectrumDaemonContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new RegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean
        public BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }

        @Bean
        public ElectrumRegtestActions electrumRegtestActions(ElectrumClient electrumClient) {
            return new ElectrumRegtestActions(electrumClient);
        }
    }

    @Autowired(required = false)
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Autowired(required = false)
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired(required = false)
    private ElectrumClient electrumClient;

    @Autowired(required = false)
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Autowired(required = false)
    private ElectrumRegtestActions electrumRegtestActions;

    private BitcoinjElectrumClient sut;

    @BeforeEach
    void setUp() {
        this.sut = new BitcoinjElectrumClientImpl(RegTestParams.get(), electrumClient);
    }

    @Test
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
    void testWalletSynchronized() {
        // wallet might need some time to be synchronized as some addresses beyond the gap limit are created in other methods
        Boolean walletSynchronized = Flux.interval(Duration.ofMillis(100))
                .map(it -> sut.delegate().isWalletSynchronized())
                .filter(it -> it)
                .blockFirst(Duration.ofSeconds(3));

        assertThat("wallet is synchronized", walletSynchronized, is(true));
    }

    @Test
    void itShouldHaveFluentSyntaxToSendBalance() {
        Stopwatch sw = Stopwatch.createStarted();
        Address address1 = sut.createNewAddress();
        Address address2 = sut.createNewAddress();

        Coin balanceOnAddress2Before = this.sut.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2Before, is(Coin.ZERO));

        AtomicReference<Sha256Hash> firstSentTxHash = new AtomicReference<>();

        Coin amountSentFromAddress1ToAddress2 = Flux.from(bitcoinRegtestActions.mineBlock())
                .flatMap(lastBlockHash -> bitcoinRegtestActions.fundAddress(() -> address1))
                .flatMap(minedBlockHashes -> electrumRegtestActions.awaitExactPayment(Coin.FIFTY_COINS, address1))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.FIFTY_COINS, address1))
                .flatMap(balanceOnAddress -> electrumRegtestActions.awaitSpendableBalance(Coin.FIFTY_COINS))
                // PAYMENT 1337 sats
                .flatMap(receivedAmount -> electrumRegtestActions.sendPayment(address2, Coin.valueOf(1337)))
                .doOnNext(firstSentTxHash::set)
                .flatMap(txId -> electrumRegtestActions.awaitTransaction(txId, 0))
                .flatMap(tx -> electrumRegtestActions.awaitExactPayment(Coin.valueOf(1337), address2))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.valueOf(1337), address2))
                .blockFirst(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        Coin balanceOnAddress2After = this.sut.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2After, is(Coin.valueOf(1337)));
        assertThat(amountSentFromAddress1ToAddress2, is(balanceOnAddress2After));

        BitcoinjUtxos addressUnspent = this.sut.getAddressUnspent(address2);
        assertThat(addressUnspent.getValue(), is(balanceOnAddress2After));
        assertThat(addressUnspent.getUtxos(), hasSize(1));

        BitcoinjUtxo firstUtxo = addressUnspent.getUtxos().stream()
                .findFirst()
                .orElseThrow();

        assertThat(firstUtxo.getTxHash(), is(firstSentTxHash.get()));
        assertThat(firstUtxo.getValue(), is(Coin.valueOf(1337)));

        Transaction transaction = this.sut.getTransaction(firstSentTxHash.get());
        TransactionOutput output = transaction.getOutput(firstUtxo.getTxPos());
        assertThat(output.getValue(), is(Coin.valueOf(1337)));

        Address addressInUtxo = output.getScriptPubKey().getToAddress(RegTestParams.get());
        assertThat(addressInUtxo, is(address2));
    }
}

