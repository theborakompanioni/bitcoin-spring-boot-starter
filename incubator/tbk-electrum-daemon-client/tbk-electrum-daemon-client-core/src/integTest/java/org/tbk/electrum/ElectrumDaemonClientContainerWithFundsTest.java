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
import org.tbk.electrum.common.WalletParams;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.electrum.faucet.SimpleElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.model.Utxos;
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
                            .walletPath(ElectrumDaemonClientContainerWithFundsTest.class.getSimpleName())
                            .password("faucet")
                            .build()
            );
        }
    }

    @Autowired
    private ElectrumRegtestFaucet electrumRegtestFaucet;

    @Autowired
    private ElectrumClient sut;

    @BeforeEach
    void tryLoadWallet() {
        try {
            log.trace("Load default wallet before test case");
            sut.loadWallet(LoadWalletParams.builder()
                    .walletPath("/home/electrum/.electrum/regtest/wallets/default_wallet")
                    .build());
        } catch (Exception e) {
            log.warn("Could not load default wallet");
        }
    }

    @BeforeEach
    void waitForWalletSynchronization() throws Exception {
        sut.waitForWalletSynchronization().get(10, TimeUnit.SECONDS);
    }

    @Test
    void testGetUtxos() {
        Utxos utxos0 = sut.getUtxos(ListUnspentParams.builder().build());
        assertThat(utxos0.getUtxos(), hasSize(0));
        assertThat(utxos0.getValue(), is(SimpleTxoValue.zero()));

        String address0 = sut.createNewAddress();

        Sha256Hash block0 = electrumRegtestFaucet.requestBitcoin(
                () -> Address.fromString(RegTestParams.get(), address0),
                Coin.valueOf(21_000)
        ).block(Duration.ofSeconds(90));

        assertThat(block0, is(notNullValue()));

        Utxos utxos1 = sut.getUtxos(ListUnspentParams.builder().build());
        assertThat(utxos1.getUtxos(), hasSize(greaterThanOrEqualTo(1)));
        // TODO: after faucet refactoring (not using one testing wallet), uncomment these
        //assertThat(utxos1.getUtxos(), hasSize(1));
        //assertThat(utxos1.getValue(), is(SimpleTxoValue.of(21_000)));
    }
}
