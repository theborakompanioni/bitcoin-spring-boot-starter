package org.tbk.bitcoin.tool.mqtt.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class BitcoinMqttServerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinMqttServerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner mainRunner(MessagePublishService<Block> bitcoinjBlockPublishService) {
        return args -> {
            AtomicLong zeromqBlockCounter = new AtomicLong();
            Flux.from(bitcoinjBlockPublishService).subscribe(arg -> {
                log.info("Received zeromq message: {} - {}", zeromqBlockCounter.incrementAndGet(), arg.getHash());
            });

            bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttRawBlockInputChannel")
    public MessageHandler mqttBlockInputChannelHandler(BitcoinSerializer bitcoinSerializer) {
        AtomicLong mqttBlockCounter = new AtomicLong();
        return message -> {
            byte[] payload = (byte[]) message.getPayload();
            Block block = bitcoinSerializer.makeBlock(payload);
            log.info("Received mqtt message  : {} - {}", mqttBlockCounter.incrementAndGet(), block.getHash());
        };
    }

}
