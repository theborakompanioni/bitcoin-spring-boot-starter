package org.tbk.bitcoin.tool.mqtt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMiner;
import org.tbk.bitcoin.tool.mqtt.config.BitcoinMqttServerAutoConfigProperties;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest(classes = {
        BitcoinMqttServerTestApplication.class,
        BitcoinMqttServerStarterTest.TestConfig.class
})
@ActiveProfiles("test")
class BitcoinMqttServerStarterTest {
    private static final BitcoinSerializer regtestBitcoinSerializer = new BitcoinSerializer(RegTestParams.get(), false);

    @Autowired(required = false)
    private BitcoinMqttServerAutoConfigProperties properties;

    @Autowired
    private BitcoindRegtestMiner bitcoindRegtestMiner;

    @Autowired
    private CapturingMessageHandler capturingMessageHandler;

    @BeforeEach
    void setUp() {
        this.capturingMessageHandler.reset();
    }

    @Test
    void propertiesPresent() {
        assertThat(properties, is(notNullValue()));

        assertThat(properties.isEnabled(), is(true));
    }

    @Test
    void canReceive() {
        assertThat(capturingMessageHandler.count(), is(0L));

        List<Sha256Hash> blockHashes = this.bitcoindRegtestMiner.mineBlocks(1);

        log.info("Waiting for mqtt /rawblock message..");
        Message<?> sentEvent = Flux.interval(Duration.ofMillis(10))
                .flatMapIterable(it -> capturingMessageHandler.elements())
                .filter(it -> "/rawblock".equals(it.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)))
                .blockFirst(Duration.ofSeconds(30));

        assertThat(sentEvent, is(notNullValue()));

        byte[] payload = (byte[]) sentEvent.getPayload();
        Block block = regtestBitcoinSerializer.makeBlock(payload);

        assertThat("the blocks mined contain the first block received via mqtt", blockHashes, hasItem(block.getHash()));
    }

    @ActiveProfiles("test")
    @Configuration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        @ServiceActivator(inputChannel = "mqttInputChannel")
        public CapturingMessageHandler capturingMessageHandler() {
            return new CapturingMessageHandler();
        }
    }

    private static class CapturingMessageHandler implements MessageHandler {

        private final Queue<Message<?>> queue = Queues.newLinkedBlockingQueue();

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            queue.add(message);
        }

        public List<Message<?>> elements() {
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
