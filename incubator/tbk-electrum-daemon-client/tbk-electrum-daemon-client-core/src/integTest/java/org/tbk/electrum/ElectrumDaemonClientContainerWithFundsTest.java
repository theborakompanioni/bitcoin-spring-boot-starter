package org.tbk.electrum;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
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
import org.tbk.bitcoin.regtest.electrum.faucet.SimpleElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.model.Utxo;
import org.tbk.electrum.model.Utxos;
import org.tbk.electrum.rpc.command.CreateNewAddressParams;
import org.tbk.electrum.rpc.command.ListUnspentParams;
import org.tbk.electrum.rpc.command.LoadWalletParams;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumDaemonClientContainerWithFundsTest {
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
        RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new RegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean
        BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }

        @Bean
        ElectrumRegtestFaucet electrumRegtestFaucet(BitcoinjElectrumClient electrumClient,
                                                    BitcoinRegtestActions bitcoinRegtestActions) {
            return new SimpleElectrumRegtestFaucet(
                    electrumClient,
                    bitcoinRegtestActions,
                    WalletParams.builder()
                            .walletPath("faucet_%s".formatted(this.getClass().getSimpleName()))
                            .password("faucet")
                            .build()
            );
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

        Sha256Hash txHash = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                Coin.valueOf(21_000)
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
        assertThat(utxo.getValue(), is(SimpleTxoValue.of(21_000)));
    }
}
