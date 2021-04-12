package org.tbk.mqtt.moquette;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import io.moquette.broker.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.event.MqttMessageDeliveredEvent;
import org.springframework.integration.mqtt.event.MqttMessageDeliveryEvent;
import org.springframework.integration.mqtt.event.MqttMessageSentEvent;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.mqtt.moquette.config.MoquetteBrokerAutoConfigProperties;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = {
        MoquetteBrokerTestApplication.class,
        MoquetteBrokerStarterTest.TestConfig.class
})
@ActiveProfiles("test")
class MoquetteBrokerStarterTest {

    @Autowired(required = false)
    private MoquetteBrokerAutoConfigProperties properties;

    @Autowired
    private Server server;

    @Autowired
    private MoquetteBrokerTestApplication.MqttTestGateway gateway;

    @Autowired
    private CapturingApplicationListener<MqttMessageDeliveryEvent> mqttMessageDeliveryEventListener;

    @BeforeEach
    void setUp() {
        this.mqttMessageDeliveryEventListener.reset();
    }

    @Test
    void propertiesPresent() {
        assertThat(properties, is(notNullValue()));

        assertThat(properties.isEnabled(), is(true));
    }

    @Test
    void canSend() {
        assertThat(mqttMessageDeliveryEventListener.count(), is(0L));

        String payload = "foo";
        gateway.send(payload);

        // according to docs, asynchronous events might be received out of order
        MqttMessageSentEvent sentEvent = Flux.interval(Duration.ofMillis(10))
                .flatMapIterable(it -> mqttMessageDeliveryEventListener.elements())
                .filter(it -> it instanceof MqttMessageSentEvent)
                .map(it -> (MqttMessageSentEvent) it)
                .blockFirst(Duration.ofSeconds(3));

        assertThat(sentEvent, is(notNullValue()));
        assertThat(sentEvent.getMessage().getPayload(), is(payload));
    }

    @Test
    void canReceive() {
        assertThat(mqttMessageDeliveryEventListener.count(), is(0L));

        String payload = "foo";
        gateway.send(payload);

        List<MqttMessageDeliveryEvent> elements = Flux.interval(Duration.ofMillis(10))
                .filter(it -> mqttMessageDeliveryEventListener.count() >= 2)
                .map(it -> mqttMessageDeliveryEventListener.elements())
                .blockFirst(Duration.ofSeconds(3));

        assertThat(elements, hasSize(greaterThanOrEqualTo(2)));

        // according to docs, asynchronous events might be received out of order
        MqttMessageSentEvent sentEvent = elements.stream()
                .filter(it -> it instanceof MqttMessageSentEvent)
                .map(it -> (MqttMessageSentEvent) it)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find MqttMessageSentEvent event"));

        assertThat(sentEvent.getMessage().getPayload(), is(payload));

        MqttMessageDeliveredEvent deliveredEvent = elements.stream()
                .filter(it -> it instanceof MqttMessageDeliveredEvent)
                .map(it -> (MqttMessageDeliveredEvent) it)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find MqttMessageDeliveredEvent event"));

        assertThat("the message delivered is the message sent", deliveredEvent.getMessageId(), is(sentEvent.getMessageId()));
    }

    @ActiveProfiles("test")
    @Configuration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        public CapturingApplicationListener<MqttMessageDeliveryEvent> mqttMessageDeliveryEventListener() {
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
