package org.tbk.bitcoin.regtest.electrum.faucet;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.Utxo;
import org.tbk.electrum.model.Utxos;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.ListUnspentParams;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleElectrumRegtestFaucetTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class SimpleElectrumRegtestFaucetTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(SimpleElectrumRegtestFaucetTestApplication.class)
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
        @Primary
        WalletParams defaultWalletParams() {
            return WalletParams.builder()
                    .walletPath("/home/electrum/.electrum/regtest/wallets/default_wallet")
                    .build();
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

    @Autowired
    private WalletParams defaultWalletParams;

    private ElectrumRegtestFaucet sut;

    @BeforeEach
    void setUp() {
        WalletParams faucetWalletParams = WalletParams.builder()
                .walletPath("faucet_%s".formatted(this.getClass().getSimpleName()))
                .password("faucet")
                .build();
        this.sut = new SimpleElectrumRegtestFaucet(electrumClient,
                bitcoinRegtestActions,
                faucetWalletParams);
    }

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(electrumClient, is(notNullValue()));
        assertThat(defaultWalletParams, is(notNullValue()));
        assertThat(electrumDaemonContainer, is(notNullValue()));
        assertThat("electrum daemon container is running", electrumDaemonContainer.isRunning(), is(true));

        assertThat(electrumxContainer, is(notNullValue()));
        assertThat("electrumx container is running", electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumDaemonContainer).block();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    @Order(10)
    void itShouldValidateMinAmount() {
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.requestBitcoin(() -> electrumClient.createNewAddress(CreateNewAddressParams.builder()
                            .walletPath(defaultWalletParams.getWalletPath())
                            .build()), Coin.ofSat(1_000).minus(Coin.SATOSHI))
                    .block(Duration.ofSeconds(30));
        });

        assertThat(e.getMessage(), is("Cannot request less than 0.00001 BTC from this faucet - got 0.00000999 BTC"));
    }

    @Test
    @Order(20)
    void itShouldValidateMaxAmount() {
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.requestBitcoin(() -> electrumClient.createNewAddress(CreateNewAddressParams.builder()
                            .walletPath(defaultWalletParams.getWalletPath())
                            .build()), Coin.ofBtc(BigDecimal.valueOf(100)).plus(Coin.SATOSHI))
                    .block(Duration.ofSeconds(30));
        });

        assertThat(e.getMessage(), is("Cannot request more than 100.00 BTC from this faucet - got 100.00000001 BTC"));
    }

    @Test
    @Order(1_000)
    void itShouldSendRequestedBitcoinToAddress() {
        Stopwatch sw = Stopwatch.createStarted();

        Address destinationAddress1 = electrumClient.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        BitcoinjBalance balanceOnDestinationAddress1Before = this.electrumClient.getAddressBalance(destinationAddress1);
        assertThat("balance of address is zero before test", balanceOnDestinationAddress1Before.getTotal(), is(Coin.ZERO));

        Utxos utxosBefore = this.electrumClient.delegate().getUtxos(ListUnspentParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());
        assertThat("wallet has no utxos before test", utxosBefore.getUtxos(), hasSize(0));

        Coin requestedAmount = Coin.FIFTY_COINS.plus(Coin.SATOSHI);
        sut.requestBitcoin(() -> destinationAddress1, requestedAmount)
                .block(Duration.ofSeconds(60));

        log.debug("Finished after {}", sw.stop());

        BitcoinjBalance balanceOnDestinationAddressAfter = this.electrumClient.getAddressBalance(destinationAddress1);
        assertThat("address has received expected amount of coins", balanceOnDestinationAddressAfter.getTotal(), is(requestedAmount));

        Utxo utxo = Flux.interval(Duration.ofMillis(100))
                .map(it -> this.electrumClient.delegate().getUtxos(ListUnspentParams.builder()
                        .walletPath(defaultWalletParams.getWalletPath())
                        .build()))
                .flatMap(it -> Flux.fromIterable(it.getUtxos()))
                .filter(it -> it.getAddress().orElse("").equals(destinationAddress1.toString()))
                .blockFirst(Duration.ofSeconds(30));
        assertThat(utxo.getValue().getValue(), is(requestedAmount.toSat()));
    }

    @Test
    @Order(1_100)
    void itShouldSendRequestedBitcoinToMultipleAddresses() {
        Stopwatch sw = Stopwatch.createStarted();

        Address destinationAddress = electrumClient.createNewAddress(CreateNewAddressParams.builder()
                .walletPath(defaultWalletParams.getWalletPath())
                .build());

        BitcoinjBalance balanceOnDestinationAddress1Before = this.electrumClient.getAddressBalance(destinationAddress);
        assertThat(balanceOnDestinationAddress1Before.getTotal(), is(Coin.ZERO));

        Coin requestedAmount = Coin.FIFTY_COINS.plus(Coin.SATOSHI);
        long numberOfRequests = 3;
        sut.requestBitcoin(() -> destinationAddress, requestedAmount)
                .repeat(numberOfRequests - 1)
                .collectList()
                .block(Duration.ofSeconds(90));

        log.debug("Finished after {}", sw.stop());

        BitcoinjBalance balanceOnDestinationAddress2After = this.electrumClient.getAddressBalance(destinationAddress);
        assertThat(balanceOnDestinationAddress2After.getTotal(), is(requestedAmount.multiply(numberOfRequests)));
    }
}
