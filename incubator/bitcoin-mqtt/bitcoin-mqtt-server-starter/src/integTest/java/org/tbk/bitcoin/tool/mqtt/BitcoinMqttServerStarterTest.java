package org.tbk.bitcoin.tool.mqtt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import io.moquette.broker.Server;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.event.MqttMessageDeliveryEvent;
import org.springframework.integration.mqtt.event.MqttMessageSentEvent;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.tool.mqtt.config.BitcoinMqttServerAutoConfigProperties;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.spring.testcontainer.bitcoind.regtest.CoinbaseRewardAddressSupplier;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest(classes = {
        BitcoinMqttServerTestApplication.class,
        BitcoinMqttServerStarterTest.TestConfig.class
})
@ActiveProfiles("test")
class BitcoinMqttServerStarterTest {

    @Autowired(required = false)
    private BitcoinMqttServerAutoConfigProperties properties;

    @Autowired
    private Server server;

    @Autowired
    private BitcoinMqttServerTestApplication.MqttTestGateway gateway;

    @Autowired
    private CapturingApplicationListener<MqttMessageSentEvent> mqttMessageSentEventListener;

    @Autowired
    private BitcoinClient bitcoinClient;

    @Autowired
    private MessagePublishService<Block> bitcoinBlockPublishService;

    @Autowired
    private CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;

    @BeforeEach
    void setUp() {
        this.mqttMessageSentEventListener.reset();
    }

    @Test
    void propertiesPresent() {
        assertThat(properties, is(notNullValue()));

        assertThat(properties.isEnabled(), is(true));
    }

    // TODO: implement this test method!
    @Test
    @Disabled("This currently does not work as 'internalPublish' will not trigger application events!")
    void canReceive() {
        assertThat(mqttMessageSentEventListener.count(), is(0L));

        Address coinbaseRewardAddress = coinbaseRewardAddressSupplier.get();

        /*Schedulers.single().schedule(() -> {
            try {
                log.info("Mine one block to trigger mqtt block message (to address {})", coinbaseRewardAddress);
                List<Sha256Hash> sha256Hashes = this.bitcoinClient.generateToAddress(1, coinbaseRewardAddress);
                log.info("Mined {} blocks with coinbase reward for address {}", sha256Hashes.size(), coinbaseRewardAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 1000, TimeUnit.MILLISECONDS);*/

        log.info("Waiting for mqtt block message..");
        MqttMessageSentEvent sentEvent = Flux.interval(Duration.ofMillis(10))
                .flatMapIterable(it -> mqttMessageSentEventListener.elements())
                .blockFirst(Duration.ofSeconds(30));

        assertThat(sentEvent, is(notNullValue()));
        assertThat(sentEvent.getMessage().getPayload(), is(notNullValue()));
    }

    @ActiveProfiles("test")
    @Configuration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        public CapturingApplicationListener<MqttMessageSentEvent> mqttMessageSentEventListener() {
            return new CapturingApplicationListener<>();
        }
    }

    private static class CapturingApplicationListener<T extends MqttMessageDeliveryEvent> implements ApplicationListener<T> {

        private final Queue<T> queue = Queues.newLinkedBlockingQueue();

        @Override
        public void onApplicationEvent(T event) {
            queue.add(event);
        }

        public List<T> elements() {
            return ImmutableList.copyOf(queue);
        }

        public long count() {
            return queue.size();
        }

        public void reset() {
            queue.clear();
        }
    }
}
