package org.tbk.bitcoin.regtest.electrum;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
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
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumRegtestScenarioTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        RegtestMiner regtestMiner(BitcoinClient bitcoinClient) {
            return new RegtestMinerImpl(bitcoinClient);
        }

        @Bean
        BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }

        @Bean
        ElectrumRegtestActions electrumRegtestActions(BitcoinjElectrumClient electrumClient) {
            return new ElectrumRegtestActions(electrumClient);
        }
    }

    @Autowired
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Autowired
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Autowired
    private ElectrumRegtestActions electrumRegtestActions;

    @Autowired
    private BitcoinjElectrumClient electrumClient;

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(electrumClient, is(notNullValue()));
        assertThat(electrumDaemonContainer, is(notNullValue()));
        assertThat("electrum daemon container is running", electrumDaemonContainer.isRunning(), is(true));

        assertThat(electrumxContainer, is(notNullValue()));
        assertThat("electrumx container is running", electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumDaemonContainer).block();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void itShouldHaveFluentSyntaxToSendBalance() {
        Stopwatch sw = Stopwatch.createStarted();

        Address address1 = electrumClient.createNewAddress();
        Address address2 = electrumClient.createNewAddress();

        Coin balanceOnAddress2Before = this.electrumClient.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2Before, is(Coin.ZERO));

        Coin amountSentFromAddress1ToAddress2 = Flux.from(bitcoinRegtestActions.mineBlock())
                .flatMap(lastBlockHash -> bitcoinRegtestActions.fundAddress(() -> address1))
                .flatMap(minedBlockHashes -> electrumRegtestActions.awaitExactPayment(Coin.FIFTY_COINS, address1))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.FIFTY_COINS, address1))
                .flatMap(balanceOnAddress -> electrumRegtestActions.awaitSpendableBalance(Coin.FIFTY_COINS))
                // FIRST PAYMENT 1000 sats
                .flatMap(receivedAmount -> electrumRegtestActions.sendPaymentAndAwaitTx(address2, Coin.valueOf(1_000L)))
                .flatMap(tx -> electrumRegtestActions.awaitExactPayment(Coin.valueOf(1_000L), address2))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.valueOf(1_000L), address2))
                // SECOND PAYMENT 2000 sats
                .flatMap(receivedAmount -> electrumRegtestActions.sendPaymentAndAwaitTx(address2, Coin.valueOf(2_000L)))
                .flatMap(tx -> electrumRegtestActions.awaitExactPayment(Coin.valueOf(2_000L), address2))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.valueOf(3_000L), address2))
                // THIRD PAYMENT 3000 sats
                .flatMap(receivedAmount -> electrumRegtestActions.sendPaymentAndAwaitTx(address2, Coin.valueOf(4_000L)))
                .flatMap(tx -> electrumRegtestActions.awaitExactPayment(Coin.valueOf(4_000L), address2))
                .flatMap(utxo -> electrumRegtestActions.awaitBalanceOnAddress(Coin.valueOf(7_000L), address2))
                .blockFirst(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        Coin balanceOnAddress2After = this.electrumClient.getAddressBalance(address2).getTotal();
        assertThat(balanceOnAddress2After, is(Coin.valueOf(1_000 + 2_000 + 4_000)));
        assertThat(amountSentFromAddress1ToAddress2, is(balanceOnAddress2After));
    }
}
