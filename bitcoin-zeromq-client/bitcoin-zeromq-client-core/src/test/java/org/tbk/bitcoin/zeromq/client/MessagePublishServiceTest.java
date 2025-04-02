package org.tbk.bitcoin.zeromq.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.common.genesis.GenesisBlock;
import org.tbk.bitcoin.zeromq.test.GenesisBlockPublisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Slf4j
public class MessagePublishServiceTest {

    @Test
    void itShouldPublishGenesisBlock() throws TimeoutException, ExecutionException, InterruptedException {
        MessagePublishService<byte[]> genesisBlockPublishService = new MessagePublishService<>(new GenesisBlockPublisher());

        CompletableFuture<List<byte[]>> blocksRef = new CompletableFuture<>();

        Flux.from(genesisBlockPublishService)
                .subscribeOn(Schedulers.single())
                .subscribe(it -> {
                    blocksRef.complete(Collections.singletonList(it));
                });

        genesisBlockPublishService.startAsync();
        genesisBlockPublishService.awaitRunning(Duration.ofSeconds(10));

        List<byte[]> blocks = blocksRef.get(20, TimeUnit.SECONDS);

        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), is(GenesisBlock.get().toByteArray()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                genesisBlockPublishService.stopAsync();
                genesisBlockPublishService.awaitTerminated(Duration.ofSeconds(10L));
            } catch (TimeoutException e) {
                log.error("", e);
            }
        }));
    }
}
