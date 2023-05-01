package org.tbk.spring.testcontainer.eps.config;

import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.SimpleBalance;
import org.tbk.spring.testcontainer.eps.ElectrumPersonalServerContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ElectrumPersonalServerContainerApplicationTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumPersonalServerContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumPersonalServerContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Configuration(proxyBeanMethods = false)
        @AutoConfigureBefore(ElectrumPersonalServerContainerAutoConfiguration.class)
        public static class ElectrumPersonalServerContainerTestConfig {

            /**
             * We must have access to a wallet for "getnewaddress" command to work.
             * Create a wallet if none is found (currently only when in regtest mode)!
             * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
             */
            @Bean
            public InitializingBean createWalletIfMissing(BitcoinExtendedClient bitcoinRegtestClient) {
                return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinRegtestClient);
            }
        }
    }

    @Autowired(required = false)
    private ElectrumPersonalServerContainer<?> container;

    @Autowired(required = false)
    private ElectrumClient electrumClient;

    @Test
    void contextLoads() {
        assertThat(container, is(notNullValue()));
        assertThat(container.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(container).block();

        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void clientIsConnected() {
        assertThat(electrumClient.isDaemonConnected(), is(true));

        // triggers a lookup on the server
        assertThat(electrumClient.getAddressBalance(electrumClient.createNewAddress()), is(SimpleBalance.zero()));
    }
}

