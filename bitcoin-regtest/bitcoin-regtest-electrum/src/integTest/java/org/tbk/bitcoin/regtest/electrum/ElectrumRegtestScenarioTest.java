package org.tbk.bitcoin.regtest.electrum;

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
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMiner;
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMinerImpl;
import org.tbk.electrum.ElectrumClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ElectrumRegtestScenarioTest {
    private static final Coin defaultTxFee = Coin.valueOf(200_000);

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public BitcoindRegtestMiner bitcoindRegtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            BitcoindRegtestMinerImpl bitcoindRegtestMiner = new BitcoindRegtestMinerImpl(bitcoinJsonRpcClient);

            // electrum daemon has problems when starting with zero blocks..
            // .. lets mine one before the tests start!
            bitcoindRegtestMiner.mineBlocks(1);

            return bitcoindRegtestMiner;
        }
    }

    @Autowired
    private ElectrumClient electrumClient;

    @Autowired
    private BitcoindRegtestMiner bitcoindRegtestMiner;

    @Test
    void itShouldVerifySimpleTransactionSendingScenario() {
        ElectrumRegtestScenarioImpl regtestScenario = new ElectrumRegtestScenarioImpl(bitcoindRegtestMiner, electrumClient);

        ElectrumRegtestScenarioImpl.RegtestFundingSource fundingSource = new ElectrumRegtestScenarioImpl.ElectrumRegtestFundingSource(electrumClient);

        Coin balanceBefore = fundingSource.getSpendableBalance();
        log.info("Balance now: {}", balanceBefore);
        assertThat("funding source balance is zero initially", balanceBefore, is(Coin.ZERO));

        Coin sendAmount1 = Coin.valueOf(1337);

        // a target address that is not controlled by the funding source
        Address targetAddress = Address.fromString(RegTestParams.get(), "bcrt1qtnh0ytrmrt6jlpfmrg9dw9cl5gzglguy62wjgg");

        regtestScenario.sendToAddress(targetAddress, sendAmount1, defaultTxFee, 1);

        Coin balanceAfterFirstSend = fundingSource.getSpendableBalance();
        log.info("Balance now: {}", balanceAfterFirstSend);

        Coin secondSendValue = Coin.valueOf(1337);
        regtestScenario.sendToAddress(targetAddress, secondSendValue, defaultTxFee, 1);

        Coin balanceAfterSecondSend = fundingSource.getSpendableBalance();
        log.info("Balance now: {}", balanceAfterSecondSend);

        Coin expectedBalance = balanceAfterFirstSend.minus(secondSendValue).minus(defaultTxFee);
        assertThat("funding source has expected balance after sending", balanceAfterSecondSend, is(expectedBalance));
    }
}
