package org.tbk.bitcoin.example.payreq.bitcoin.block;

import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
class BitcoinBlockUpdateListener implements InitializingBean, DisposableBean {

    @NonNull
    private final MessagePublishService<Block> bitcoinBlockPublishService;

    @NonNull
    private final BitcoinClient bitcoinClient;

    @NonNull
    private final BitcoinBlockService blockService;

    private Disposable subscription;

    @Override
    public void afterPropertiesSet() throws TimeoutException {
        bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
        this.subscription = Flux.from(bitcoinBlockPublishService).subscribe(block -> {
            try {
                BlockInfo info = bitcoinClient.getBlockInfo(block.getHash());
                blockService.createBlock(info);
            } catch (IOException e) {
                log.error("error while fetching 'blockinfo' via bitcoin api", e);
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

    @Override
    public void destroy() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
