package org.tbk.bitcoin.tool.mqtt.example;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.moquette.BrokerConstants;
import io.moquette.broker.config.MemoryConfig;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
public class BitcoinMqttServerExampleApplicationConfig {

    private final MemoryConfig memoryConfig;

    public BitcoinMqttServerExampleApplicationConfig(MemoryConfig memoryConfig) {
        this.memoryConfig = requireNonNull(memoryConfig);
    }

    private static HikariConfig createHikariConfig(String database) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SQLitePool");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(3);
        config.setDriverClassName(org.sqlite.JDBC.class.getName());
        config.setJdbcUrl("jdbc:sqlite:" + database + ".db");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(TimeUnit.SECONDS.toMillis(60));
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(45));
        return config;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = createHikariConfig("bitcoin_mqtt_server_example_application");
        return new HikariDataSource(config);
    }

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer(ApplicationContext applicationContext) {
        return configuration -> {
            JavaMigration[] javaMigrations = applicationContext.getBeansOfType(JavaMigration.class)
                    .values().toArray(JavaMigration[]::new);

            configuration.javaMigrations(javaMigrations);
        };
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        String serverUri = String.format("tcp://%s:%s",
                memoryConfig.getProperty(BrokerConstants.HOST_PROPERTY_NAME),
                memoryConfig.getProperty(BrokerConstants.PORT_PROPERTY_NAME));

        options.setServerURIs(new String[]{serverUri});
        options.setUserName("user");
        options.setPassword("bitcoin".toCharArray());

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
        ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("mqtt-rawblock-pub-%d")
                .setDaemon(false)
                .build());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MoreExecutors.shutdownAndAwaitTermination(executorService, Duration.ofSeconds(10));
        }));

        return new PublishSubscribeChannel(executorService);
    }

    @Bean
    public MessageProducer mqttRawBlockInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("mqttBlockConsumer", mqttClientFactory(), "/rawblock");
        adapter.setCompletionTimeout(Duration.ofSeconds(5).toMillis());
        adapter.setConverter(defaultPahoMessageConverter());
        adapter.setQos(MqttQoS.AT_LEAST_ONCE.value());
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
