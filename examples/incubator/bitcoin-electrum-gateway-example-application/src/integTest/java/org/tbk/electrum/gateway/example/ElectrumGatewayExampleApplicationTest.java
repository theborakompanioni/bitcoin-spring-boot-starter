package org.tbk.electrum.gateway.example;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.ElectrumClientFactory;
import org.tbk.electrum.ElectrumClientFactoryImpl;
import org.tbk.electrum.command.DaemonStatusResponse;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcConfig;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcConfigBuilder;
import org.tbk.electrum.model.Balance;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumd.config.SimpleElectrumDaemonContainerFactory;
import org.tbk.spring.testcontainer.electrumd.config.SimpleElectrumDaemonContainerFactory.ElectrumDaemonContainerConfig;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest(classes = {
        ElectrumGatewayExampleApplication.class,
        ElectrumGatewayExampleApplicationTest.TestConfig.class
})
@ActiveProfiles("test")
public class ElectrumGatewayExampleApplicationTest {

    @Autowired(required = false)
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired(required = false)
    @Qualifier("primaryElectrumDaemonContainer")
    private ElectrumDaemonContainer<?> primaryElectrumDaemonContainer;

    @Autowired(required = false)
    @Qualifier("primaryElectrumClient")
    public ElectrumClient primaryElectrumClient;

    @Autowired(required = false)
    @Qualifier("secondaryElectrumDaemonContainer")
    private ElectrumDaemonContainer<?> secondaryElectrumDaemonContainer;

    @Autowired(required = false)
    @Qualifier("secondaryElectrumClient")
    public ElectrumClient secondaryElectrumClient;

    @Test
    void contextLoads() {
        assertThat(electrumxContainer, is(notNullValue()));

        assertThat(primaryElectrumDaemonContainer, is(notNullValue()));
        assertThat(primaryElectrumClient, is(notNullValue()));

        assertThat(secondaryElectrumDaemonContainer, is(notNullValue()));
        assertThat(secondaryElectrumClient, is(notNullValue()));
    }

    @Test
    void electrumxContainerStarted() {
        assertThat(electrumxContainer, is(notNullValue()));
        assertThat(electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumxContainer).blockFirst();

        assertThat("electrumx ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void primaryElectrumDaemonContainerStarted() {
        assertThat(primaryElectrumDaemonContainer, is(notNullValue()));
        assertThat(primaryElectrumDaemonContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(primaryElectrumDaemonContainer).blockFirst();

        assertThat("primary electrumd container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));

        DaemonStatusResponse daemonStatusResponse = primaryElectrumClient.daemonStatus();
        assertThat(daemonStatusResponse.isConnected(), is(true));
        assertThat(primaryElectrumClient.isWalletSynchronized(), is(true));
    }

    @Test
    void secondaryElectrumDaemonContainerStarted() {
        assertThat(secondaryElectrumDaemonContainer, is(notNullValue()));
        assertThat(secondaryElectrumDaemonContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(secondaryElectrumDaemonContainer).blockFirst();

        assertThat("secondary electrumd container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));

        DaemonStatusResponse daemonStatusResponse = secondaryElectrumClient.daemonStatus();
        assertThat(daemonStatusResponse.isConnected(), is(true));
        assertThat(secondaryElectrumClient.isWalletSynchronized(), is(true));
    }

    @Test
    void verifyTargetWalletReceivesCoins() {
        Stopwatch sw = Stopwatch.createStarted();

        Balance initialBalanceOfSecondaryWallet = secondaryElectrumClient.getBalance();
        Coin initialSpendableValue = Coin.valueOf(initialBalanceOfSecondaryWallet.getSpendable().getValue());

        log.info("Starting with balance {} on target wallet", initialSpendableValue.toFriendlyString());

        // poll every 1s for at most 60s till second_wallet received coins
        // with blocks mined every second, electrum takes ~30s to fully synchronize and "see" updated balances
        Balance finalBalanceOfSecondaryWallet = Flux.interval(Duration.ofSeconds(1))
                .map(foo -> secondaryElectrumClient.getBalance())
                .filter(currentBalance -> currentBalance.getSpendable().getValue() > initialSpendableValue.getValue())
                .blockFirst(Duration.ofSeconds(60));

        assertThat(finalBalanceOfSecondaryWallet, is(notNullValue()));
        Coin finalSpendableValue = Coin.valueOf(finalBalanceOfSecondaryWallet.getSpendable().getValue());

        log.info("Found new balance {} on target wallet after {}", finalSpendableValue.toFriendlyString(), sw.stop());

        assertThat("spendable value of secondary wallet increased", finalSpendableValue.isGreaterThan(initialSpendableValue), is(true));
    }

    /**
     * Setup 2 electrum daemon containers + 2 electrum clients connecting to them individually.
     */
    @ActiveProfiles("test")
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureAfter(ElectrumxContainerAutoConfiguration.class)
    public static class TestConfig {

        @Bean
        public SimpleElectrumDaemonContainerFactory electrumDaemonContainerFactory() {
            return new SimpleElectrumDaemonContainerFactory();
        }

        @Primary
        @Bean("primaryElectrumDaemonContainer")
        public ElectrumDaemonContainer<?> primaryElectrumDaemonContainer(SimpleElectrumDaemonContainerFactory electrumDaemonContainerFactory,
                                                                         ElectrumxContainer<?> electrumxContainer) {

            ElectrumDaemonContainerConfig containerConfig = ElectrumDaemonContainerConfig.builder()
                    .defaultWallet("electrum/wallets/regtest/default_wallet")
                    .addEnvVar("ELECTRUM_USER", "electrum")
                    .addEnvVar("ELECTRUM_PASSWORD", "correct_horse_battery_staple_20210516-0")
                    .build();

            return electrumDaemonContainerFactory.createStartedElectrumDaemonContainer(containerConfig, electrumxContainer);
        }

        @Bean("secondaryElectrumDaemonContainer")
        public ElectrumDaemonContainer<?> secondaryElectrumDaemonContainer(SimpleElectrumDaemonContainerFactory electrumDaemonContainerFactory,
                                                                           ElectrumxContainer<?> electrumxContainer) {

            ElectrumDaemonContainerConfig containerConfig = ElectrumDaemonContainerConfig.builder()
                    .defaultWallet("electrum/wallets/regtest/second_wallet")
                    .addEnvVar("ELECTRUM_USER", "electrum")
                    .addEnvVar("ELECTRUM_PASSWORD", "correct_horse_battery_staple_20210516-1")
                    .build();

            return electrumDaemonContainerFactory.createStartedElectrumDaemonContainer(containerConfig, electrumxContainer);
        }

        @Bean
        public ElectrumClientFactory electrumClientFactory() {
            return new ElectrumClientFactoryImpl();
        }

        @Primary
        @Bean("primaryElectrumClient")
        public ElectrumClient primaryElectrumClient(ElectrumClientFactory electrumClientFactory,
                                                    @Qualifier("primaryElectrumDaemonContainer") ElectrumDaemonContainer<?> electrumDaemonContainer) {
            ElectrumDaemonJsonrpcConfig config = new ElectrumDaemonJsonrpcConfigBuilder()
                    .host("http://" + electrumDaemonContainer.getHost())
                    .port(electrumDaemonContainer.getMappedPort(7000))
                    .username("electrum")
                    .password("correct_horse_battery_staple_20210516-0")
                    .build();

            return electrumClientFactory.create(config.getUri(), config.getUsername(), config.getPassword());
        }

        @Bean("secondaryElectrumClient")
        public ElectrumClient secondaryElectrumClient(ElectrumClientFactory electrumClientFactory,
                                                      @Qualifier("secondaryElectrumDaemonContainer") ElectrumDaemonContainer<?> electrumDaemonContainer) {
            ElectrumDaemonJsonrpcConfig config = new ElectrumDaemonJsonrpcConfigBuilder()
                    .host("http://" + electrumDaemonContainer.getHost())
                    .port(electrumDaemonContainer.getMappedPort(7000))
                    .username("electrum")
                    .password("correct_horse_battery_staple_20210516-1")
                    .build();

            return electrumClientFactory.create(config.getUri(), config.getUsername(), config.getPassword());
        }
    }
}
