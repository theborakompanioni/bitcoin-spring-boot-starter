package org.tbk.electrum;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.AddressCallbackElectrumClientContainerTest.ElectrumDaemonContainerTestApplication.TestCtrl;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.model.SimpleTxoValue;
import org.tbk.electrum.model.TxoValue;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.Testcontainers;
import reactor.core.publisher.Flux;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddressCallbackElectrumClientContainerTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumDaemonContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonContainerTestApplication.class)
                    .web(WebApplicationType.SERVLET)
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
        ElectrumRegtestActions electrumRegtestActions(BitcoinjElectrumClient electrumClient) {
            return new ElectrumRegtestActions(electrumClient);
        }

        @RestController
        public static class TestCtrl {
            private final static AtomicReference<CallbackPayload> lastBody = new AtomicReference<>();

            public static Optional<CallbackPayload> getLastBody() {
                return Optional.ofNullable(lastBody.get());
            }

            public static Optional<CallbackPayload> getAndResetLastBody() {
                return Optional.ofNullable(lastBody.getAndSet(null));
            }

            @PostMapping("/api/test-callback")
            public void postCallback(@RequestBody CallbackPayload body) {
                lastBody.set(body);
            }

            @lombok.Value
            @Builder
            @Jacksonized
            public static class CallbackPayload {
                @NonNull
                String address;

                @Nullable
                String status;

                public Optional<String> getStatus() {
                    return Optional.ofNullable(status);
                }
            }
        }
    }

    @Autowired
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Autowired
    private ElectrumRegtestActions electrumRegtestActions;

    @Autowired
    private ElectrumClient sut;

    @Value(value = "${local.server.port}")
    private int port;

    @BeforeEach
    void exposeHostPorts() {
        Testcontainers.exposeHostPorts(port);
    }

    @BeforeEach
    void waitForWalletSynchronization() throws Exception {
        sut.waitForWalletSynchronization().get(10, TimeUnit.SECONDS);
    }

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(sut, is(notNullValue()));
    }

    @Test
    void itShouldAddChangeAddressListener() {
        assertThat(TestCtrl.getLastBody().isPresent(), is(false));

        String address1 = sut.createNewAddress();

        TxoValue balanceOnAddress1Before = this.sut.getAddressBalance(address1).getTotal();
        assertThat(balanceOnAddress1Before, is(SimpleTxoValue.zero()));

        URI callbackUrl = URI.create("http://%s:%d/api/test-callback".formatted(MoreTestcontainers.testcontainersInternalHost(), port));
        Boolean callbackRegisteredSuccessfully = sut.addAddressChangedNotificationCallback(address1, callbackUrl);
        assertThat(callbackRegisteredSuccessfully, is(true));

        TestCtrl.CallbackPayload callbackPayload0 = Flux.interval(Duration.ofMillis(100))
                .filter(it -> TestCtrl.getLastBody().isPresent())
                .map(it -> TestCtrl.getAndResetLastBody().orElseThrow())
                .blockFirst(Duration.ofSeconds(30));

        assertThat("callback is initially invoked for address", callbackPayload0, is(notNullValue()));
        assertThat("address is present in initial payload", callbackPayload0.getAddress(), is(address1));
        assertThat("status is empty in initial payload", callbackPayload0.getStatus().isPresent(), is(false));

        Flux.from(bitcoinRegtestActions.mineBlock())
                .flatMap(lastBlockHash -> bitcoinRegtestActions.fundAddress(() -> Address.fromString(RegTestParams.get(), address1)))
                .blockFirst(Duration.ofSeconds(90));

        TestCtrl.CallbackPayload callbackPayload1 = Flux.interval(Duration.ofMillis(100))
                .filter(it -> TestCtrl.getLastBody().isPresent())
                .map(it -> TestCtrl.getAndResetLastBody().orElseThrow())
                .blockFirst(Duration.ofSeconds(90));

        assertThat(callbackPayload1, is(notNullValue()));
        assertThat(callbackPayload1.getAddress(), is(address1));
        assertThat("status has a value", callbackPayload1.getStatus().isPresent(), is(true));

        Boolean callbackUnregisteredSuccessfully = sut.removeAddressChangedNotificationCallback(address1);
        assertThat(callbackUnregisteredSuccessfully, is(true));
    }

}

