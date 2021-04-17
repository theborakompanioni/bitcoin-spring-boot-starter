package org.tbk.bitcoin.tool.mqtt.example;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
public class BitcoinMqttServerExampleApplicationConfig {

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://localhost:18833"});
        options.setUserName("username");
        options.setPassword("password".toCharArray());
        return options;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    /* INBOUND */
    @Bean
    public MessageChannel mqttRawBlockInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttRawBlockInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("mqttBlockConsumer", mqttClientFactory(), "/rawblock");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(defaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setAutoStartup(true);
        adapter.setManualAcks(false);
        adapter.setOutputChannel(mqttRawBlockInputChannel());
        return adapter;
    }

    @Bean
    public DefaultPahoMessageConverter defaultPahoMessageConverter() {
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        return converter;
    }
    /* INBOUND - end */
}
