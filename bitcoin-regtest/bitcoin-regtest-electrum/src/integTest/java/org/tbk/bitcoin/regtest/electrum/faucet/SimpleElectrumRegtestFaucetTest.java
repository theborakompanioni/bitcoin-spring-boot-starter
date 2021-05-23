package org.tbk.bitcoin.regtest.electrum.faucet;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleElectrumRegtestFaucetTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public RegtestMiner regtestMiner(BitcoinClient bitcoinClient) {
            return new RegtestMinerImpl(bitcoinClient);
        }

        @Bean
        public BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }
    }

    @Autowired
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Autowired
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Autowired
    private BitcoinjElectrumClient electrumClient;

    private ElectrumRegtestFaucet sut;

    @BeforeEach
    void setUp() {
        this.sut = new SimpleElectrumRegtestFaucet(electrumClient, bitcoinRegtestActions);
    }

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
    @Order(1001)
    void itShouldSendRequestedBitcoinToAddress() {
        Stopwatch sw = Stopwatch.createStarted();

        Address destinationAddress1 = electrumClient.createNewAddress();

        BitcoinjBalance balanceOnDestinationAddress1Before = this.electrumClient.getAddressBalance(destinationAddress1);
        assertThat("balance of addres is zero before test", balanceOnDestinationAddress1Before.getTotal(), is(Coin.ZERO));

        sut.requestBitcoin(() -> destinationAddress1, Coin.FIFTY_COINS.plus(Coin.SATOSHI))
                .block(Duration.ofSeconds(60));

        log.debug("Finished after {}", sw.stop());

        BitcoinjBalance balanceOnDestinationAddressAfter = this.electrumClient.getAddressBalance(destinationAddress1);
        assertThat("address has received expected amount of coins", balanceOnDestinationAddressAfter.getTotal(), is(Coin.FIFTY_COINS.plus(Coin.SATOSHI)));
    }

    @Test
    @Order(1002)
    void itShouldSendRequestedBitcoinToMultipleAddressesWithoutNeedingNewCoinbaseRewardsInbetween() {
        Stopwatch sw = Stopwatch.createStarted();

        // if this test is called in isolation, this request will trigger the faucet to mine some blocks
        // if other test methods run before the faucet will already be in control of enough funds
        sut.requestBitcoin(() -> electrumClient.createNewAddress(), Coin.SATOSHI.multiply(1000))
                .block(Duration.ofSeconds(60));

        int blockchainHeightBefore = electrumClient.delegate().daemonStatus().getBlockchainHeight();
        assertThat("blocks have already been mined", blockchainHeightBefore, is(greaterThan(0)));

        Address destinationAddress = electrumClient.createNewAddress();

        BitcoinjBalance balanceOnDestinationAddress1Before = this.electrumClient.getAddressBalance(destinationAddress);
        assertThat(balanceOnDestinationAddress1Before.getTotal(), is(Coin.ZERO));

        sut.requestBitcoin(() -> destinationAddress, Coin.SATOSHI.multiply(1000))
                .repeat(2)
                .collectList()
                .block(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        BitcoinjBalance balanceOnDestinationAddress2After = this.electrumClient.getAddressBalance(destinationAddress);
        assertThat(balanceOnDestinationAddress2After.getTotal(), is(Coin.SATOSHI.multiply(1000).multiply(3)));

        int blockchainHeightAfter = electrumClient.delegate().daemonStatus().getBlockchainHeight();
        assertThat("no additional blocks have been mined to fund the faucet", blockchainHeightAfter, is(blockchainHeightBefore));
    }
}
