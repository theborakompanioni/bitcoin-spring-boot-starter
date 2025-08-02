package org.tbk.bitcoin.regtest.electrum.faucet;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
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
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.CreateParams;
import org.tbk.electrum.rpc.command.GetBalanceParams;
import org.tbk.electrum.rpc.command.LoadWalletParams;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleElectrumRegtestFaucetWithMultipleWalletsTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class SimpleElectrumRegtestFaucetWithMultipleWalletsTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(SimpleElectrumRegtestFaucetWithMultipleWalletsTestApplication.class)
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
        this.sut = new SimpleElectrumRegtestFaucet(electrumClient,
                bitcoinRegtestActions,
                WalletParams.builder()
                        .walletPath("faucet_%s".formatted(this.getClass().getSimpleName()))
                        .password("faucet")
                        .build());
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
    @Order(1_000)
    void itShouldFundMultipleWallets() {
        String walletPrefix = SimpleElectrumRegtestFaucetWithMultipleWalletsTest.class.getSimpleName();

        List<WalletParams> walletParamList = createAndLoadWalletsOrThrow(Stream.of(0, 1, 2)
                .map(i -> WalletParams.builder()
                        .walletPath("%s_%d".formatted(walletPrefix, i))
                        .password("ANY_PASSWORD_%d".formatted(i))
                        .build())
                .toList());

        String newAddress0 = electrumClient.delegate().createNewAddress(CreateNewAddressParams.builder()
                .walletPath(walletParamList.get(0).getWalletPath())
                .build());
        Balance addressBalance0 = electrumClient.delegate().getAddressBalance(newAddress0);
        assertThat("balance is zero", addressBalance0.getTotal(), is(SimpleTxoValue.zero()));

        String newAddress1 = electrumClient.delegate().createNewAddress(CreateNewAddressParams.builder()
                .walletPath(walletParamList.get(1).getWalletPath())
                .build());
        Balance addressBalance1 = electrumClient.delegate().getAddressBalance(newAddress1);
        assertThat("balance is zero", addressBalance1.getTotal(), is(SimpleTxoValue.zero()));

        String newAddress2 = electrumClient.delegate().createNewAddress(CreateNewAddressParams.builder()
                .walletPath(walletParamList.get(2).getWalletPath())
                .build());
        Balance addressBalance2 = electrumClient.delegate().getAddressBalance(newAddress2);
        assertThat("balance is zero", addressBalance2.getTotal(), is(SimpleTxoValue.zero()));

        Flux.concat(
                sut.requestBitcoin(() -> Address.fromString(RegTestParams.get(), newAddress0), Coin.valueOf(21_000)),
                sut.requestBitcoin(() -> Address.fromString(RegTestParams.get(), newAddress1), Coin.valueOf(42_000)),
                sut.requestBitcoin(() -> Address.fromString(RegTestParams.get(), newAddress2), Coin.valueOf(84_000))
        ).blockLast(Duration.ofSeconds(60));

        Balance addressBalanceAfter0 = electrumClient.delegate().getAddressBalance(newAddress0);
        Balance walletBalanceAfter0 = electrumClient.delegate().getBalance(GetBalanceParams.builder()
                .walletPath(walletParamList.get(0).getWalletPath())
                .build());
        assertThat("address0 now funded", addressBalanceAfter0.getTotal(), is(SimpleTxoValue.of(21_000)));
        assertThat("wallet0 now funded", walletBalanceAfter0.getTotal(), is(SimpleTxoValue.of(21_000)));

        Balance addressBalanceAfter1 = electrumClient.delegate().getAddressBalance(newAddress1);
        Balance walletBalanceAfter1 = electrumClient.delegate().getBalance(GetBalanceParams.builder()
                .walletPath(walletParamList.get(1).getWalletPath())
                .build());
        assertThat("address1 now funded", addressBalanceAfter1.getTotal(), is(SimpleTxoValue.of(42_000)));
        assertThat("wallet1 now funded", walletBalanceAfter1.getTotal(), is(SimpleTxoValue.of(42_000)));

        Balance addressBalanceAfter2 = electrumClient.delegate().getAddressBalance(newAddress2);
        Balance walletBalanceAfter2 = electrumClient.delegate().getBalance(GetBalanceParams.builder()
                .walletPath(walletParamList.get(2).getWalletPath())
                .build());
        assertThat("address2 now funded", addressBalanceAfter2.getTotal(), is(SimpleTxoValue.of(84_000)));
        assertThat("wallet1 now funded", walletBalanceAfter2.getTotal(), is(SimpleTxoValue.of(84_000)));
    }

    private List<WalletParams> createAndLoadWalletsOrThrow(List<WalletParams> walletParamList) {
        walletParamList.forEach(it -> {
            electrumClient.delegate().createWallet(CreateParams.builder()
                    .walletPath(it.getWalletPath())
                    .password(it.getPassword().orElse(null))
                    .encryptFile(it.getPassword().isPresent())
                    .build());
        });

        walletParamList.forEach(it -> {
            electrumClient.delegate().loadWallet(LoadWalletParams.builder()
                    .walletPath(it.getWalletPath())
                    .password(it.getPassword().orElse(null))
                    .build());
        });

        return walletParamList;
    }
}
