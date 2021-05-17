package org.tbk.bitcoin.regtest.electrum;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.common.AddressSupplier;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.TxoValue;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
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
        public RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            RegtestMinerImpl regtestMiner = new RegtestMinerImpl(bitcoinJsonRpcClient);

            // electrum daemon has problems when starting with zero blocks..
            // .. lets mine one before the tests start!
            regtestMiner.mineBlocks(1);

            return regtestMiner;
        }

        @Bean
        public BitcoinRegtestActions bitcoinScenarioFactory(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }

        @Bean
        public ElectrumRegtestActions electrumScenarioFactory(ElectrumClient electrumClient) {
            return new ElectrumRegtestActions(electrumClient);
        }
    }

    @Autowired
    private BitcoinRegtestActions bitcoinScenarioFactory;

    @Autowired
    private ElectrumRegtestActions electrumScenarioFactory;

    @Autowired
    private ElectrumClient electrumClient;

    @Test
    void itShouldHaveFluentSyntaxToSendBalance() {
        Stopwatch sw = Stopwatch.createStarted();

        AddressSupplier createNewAddress = () -> {
            String address = this.electrumClient.createNewAddress();
            return Address.fromString(RegTestParams.get(), address);
        };

        Address address1 = createNewAddress.get();
        Address address2 = createNewAddress.get();

        Coin balanceOnAddress2Before = toCoin(this.electrumClient.getAddressBalance(address2.toString()).getTotal());
        assertThat(balanceOnAddress2Before, is(Coin.ZERO));

        Coin amountSentFromAddress1ToAddress2 = Flux.from(bitcoinScenarioFactory.mineBlock())
                .flatMap(lastBlockHash -> bitcoinScenarioFactory.fundAddress(() -> address1))
                .flatMap(minedBlockHashes -> electrumScenarioFactory.awaitExactPayment(Coin.FIFTY_COINS, address1))
                .flatMap(utxo -> electrumScenarioFactory.awaitBalanceOnAddress(Coin.FIFTY_COINS, address1))
                .flatMap(balanceOnAddress -> electrumScenarioFactory.awaitSpendableBalance(Coin.FIFTY_COINS))
                // FIRST PAYMENT 1000 sats
                .flatMap(receivedAmount -> electrumScenarioFactory.sendPayment(address2, Coin.valueOf(1000L)))
                .flatMap(txId -> electrumScenarioFactory.awaitTransaction(txId, 0))
                .flatMap(tx -> electrumScenarioFactory.awaitExactPayment(Coin.valueOf(1000L), address2))
                .flatMap(utxo -> electrumScenarioFactory.awaitBalanceOnAddress(Coin.valueOf(1000L), address2))
                // SECOND PAYMENT 2000 sats
                .flatMap(receivedAmount -> electrumScenarioFactory.sendPayment(address2, Coin.valueOf(2000L)))
                .flatMap(txId -> electrumScenarioFactory.awaitTransaction(txId, 0))
                .flatMap(tx -> electrumScenarioFactory.awaitExactPayment(Coin.valueOf(2000L), address2))
                .flatMap(utxo -> electrumScenarioFactory.awaitBalanceOnAddress(Coin.valueOf(3000L), address2))
                // THIRD PAYMENT 3000 sats
                .flatMap(receivedAmount -> electrumScenarioFactory.sendPayment(address2, Coin.valueOf(4000L)))
                .flatMap(txId -> electrumScenarioFactory.awaitTransaction(txId, 0))
                .flatMap(tx -> electrumScenarioFactory.awaitExactPayment(Coin.valueOf(4000L), address2))
                .flatMap(utxo -> electrumScenarioFactory.awaitBalanceOnAddress(Coin.valueOf(7000L), address2))
                .blockFirst(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        Coin balanceOnAddress2After = toCoin(this.electrumClient.getAddressBalance(address2.toString()).getTotal());
        assertThat(balanceOnAddress2After, is(Coin.valueOf(1000 + 2000 + 4000)));
        assertThat(amountSentFromAddress1ToAddress2, is(balanceOnAddress2After));
    }

    private Coin toCoin(TxoValue val) {
        return Coin.valueOf(val.getValue());
    }

}
