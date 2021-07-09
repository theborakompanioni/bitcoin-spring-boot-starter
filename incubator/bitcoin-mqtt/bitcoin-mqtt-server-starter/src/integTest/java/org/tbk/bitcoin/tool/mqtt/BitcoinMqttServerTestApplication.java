package org.tbk.bitcoin.tool.mqtt;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
class BitcoinMqttServerTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinMqttServerTestApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://localhost:18832"});
        options.setUserName("username");
        options.setPassword("password".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    /* INBOUND */
    @Bean
    public MessageChannel mqttInputChannel() {
        ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("mqtt-input-pub-%d")
                .setDaemon(false)
                .build());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MoreExecutors.shutdownAndAwaitTermination(executorService, Duration.ofSeconds(10));
        }));

        return new PublishSubscribeChannel(executorService);
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("testConsumer", mqttClientFactory(), "#");
        adapter.setCompletionTimeout(Duration.ofSeconds(5).toMillis());
        adapter.setConverter(defaultPahoMessageConverter());
        adapter.setQos(MqttQoS.AT_LEAST_ONCE.value());
        adapter.setAutoStartup(true);
        adapter.setManualAcks(false);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public DefaultPahoMessageConverter defaultPahoMessageConverter() {
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        return converter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttInputChannelHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                log.info("Received mqtt message: {}", message);
            }
        };
    }
    /* INBOUND - end */

    /* OUTBOUND */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("testPublisher", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setAsyncEvents(true);
        messageHandler.setDefaultTopic("testTopic");
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("mqtt-output-pub-%d")
                .setDaemon(false)
                .build());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MoreExecutors.shutdownAndAwaitTermination(executorService, Duration.ofSeconds(10));
        }));

        return new PublishSubscribeChannel(executorService);
    }
    /* OUTBOUND - end */

    @MessagingGateway(
            defaultRequestChannel = "mqttOutboundChannel",
            asyncExecutor = "mqttGatewayTaskExecutor"
    )
    public interface MqttTestGateway {

        void send(String data);

    }

    @Bean
    public AsyncTaskExecutor mqttGatewayTaskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setThreadNamePrefix("mqtt-gateway-");
        return simpleAsyncTaskExecutor;
    }

}
