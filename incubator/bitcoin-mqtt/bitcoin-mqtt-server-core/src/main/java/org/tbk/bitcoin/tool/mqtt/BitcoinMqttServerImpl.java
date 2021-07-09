package org.tbk.bitcoin.tool.mqtt;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.moquette.broker.Server;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.requireNonNull;

@Slf4j
public class BitcoinMqttServerImpl extends AbstractIdleService implements BitcoinMqttServer {

    private final String serviceId = Integer.toHexString(System.identityHashCode(this));

    private final ExecutorService publisherExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("zmq-msg-pub-" + serviceId + "-%d")
            .setDaemon(false)
            .build());

    private final Scheduler subscribeOnScheduler = Schedulers.newSingle("mqtt-msg-sub-" + serviceId);

    private final String clientId;
    private final Server mqttServer;
    private final MessagePublishService<Block> blockMessagePublisherService;
    private final MessagePublishService<Transaction> transactionMessagePublisherService;

    private Disposable blockSubscription;
    private Disposable transactionSubscription;

    public BitcoinMqttServerImpl(String clientId,
                                 Server mqttServer,
                                 MessagePublishService<Block> blockMessagePublisherService,
                                 MessagePublishService<Transaction> transactionMessagePublisherService) {
        requireNonNull(clientId);
        checkArgument(!clientId.isBlank(), "'clientId' must not be blank.");

        this.clientId = requireNonNull(clientId);
        this.mqttServer = requireNonNull(mqttServer);
        this.blockMessagePublisherService = requireNonNull(blockMessagePublisherService);
        this.transactionMessagePublisherService = requireNonNull(transactionMessagePublisherService);
    }

    @Override
    protected void startUp() {
        log.info("starting..");

        this.blockSubscription = Flux.from(blockMessagePublisherService)
                .subscribeOn(subscribeOnScheduler)
                .subscribe(block -> {
                    this.mqttServer.internalPublish(MqttMessageBuilders.publish()
                            .topicName("/hashblock")
                            .retained(true) // last hash should be retained (even if it might not be the latest block hash)
                            .qos(MqttQoS.AT_LEAST_ONCE)
                            .payload(Unpooled.copiedBuffer(block.getHash().getBytes()))
                            .build(), this.clientId);

                    this.mqttServer.internalPublish(MqttMessageBuilders.publish()
                            .topicName("/rawblock")
                            .retained(true) // last block should be retained (even if it might not be the latest block)
                            .qos(MqttQoS.AT_LEAST_ONCE)
                            .payload(Unpooled.copiedBuffer(block.bitcoinSerialize()))
                            .build(), this.clientId);
                });

        this.transactionSubscription = Flux.from(transactionMessagePublisherService)
                .subscribeOn(subscribeOnScheduler)
                .subscribe(transaction -> {
                    this.mqttServer.internalPublish(MqttMessageBuilders.publish()
                            .topicName("/hashtx")
                            .retained(false)
                            .qos(MqttQoS.AT_LEAST_ONCE)
                            .payload(Unpooled.copiedBuffer(transaction.getTxId().getBytes()))
                            .build(), this.clientId);

                    this.mqttServer.internalPublish(MqttMessageBuilders.publish()
                            .topicName("/rawtx")
                            .retained(false)
                            .qos(MqttQoS.AT_LEAST_ONCE)
                            .payload(Unpooled.copiedBuffer(transaction.bitcoinSerialize()))
                            .build(), this.clientId);
                });

        log.info("started successfully");
    }

    @Override
    protected void shutDown() {
        log.info("terminating..");

        this.blockSubscription.dispose();
        this.transactionSubscription.dispose();

        boolean executorShutdownSuccessful = shutdownAndAwaitTermination(publisherExecutor, Duration.ofSeconds(10));
        if (!executorShutdownSuccessful) {
            log.warn("unclean shutdown of executor service");
        }

        log.info("terminated");
    }
}
