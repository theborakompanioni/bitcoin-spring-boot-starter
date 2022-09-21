package org.tbk.bitcoin.regtest.common;

import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.json.pojo.TxOutSetInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
public final class BitcoindStatusLogging {

    private BitcoindStatusLogging() {
        throw new UnsupportedOperationException();
    }

    public static Disposable logBitcoinStatusOnNewBlock(MessagePublishService<Block> bitcoinjBlockPublishService,
                                                        BitcoinClient bitcoinClient) throws TimeoutException {
        Disposable subscription = Flux.from(bitcoinjBlockPublishService).subscribe(arg -> logStatus(bitcoinClient));

        Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));

        bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));

        return subscription;
    }

    public static void logStatus(BitcoinClient bitcoinClient) {
        try {
            TxOutSetInfo txOutSetInfo = bitcoinClient.getTxOutSetInfo();
            NetworkInfo networkInfo = bitcoinClient.getNetworkInfo();
            BlockChainInfo blockChainInfo = bitcoinClient.getBlockChainInfo();
            log.info("============================");
            log.info("Bitcoin Core ({}) Status", networkInfo.getSubVersion());
            log.info("Chain: {}", blockChainInfo.getChain());
            log.info("Connections: {}", networkInfo.getConnections());
            log.info("Headers: {}", blockChainInfo.getHeaders());
            log.info("Blocks: {}", blockChainInfo.getBlocks());
            log.info("Best block hash: {}", blockChainInfo.getBestBlockHash());
            log.info("Difficulty: {}", blockChainInfo.getDifficulty().toPlainString());
            log.info("UTXO: {} ({})", txOutSetInfo.getTransactions(), txOutSetInfo.getTotalAmount().toFriendlyString());
            log.info("============================");
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
