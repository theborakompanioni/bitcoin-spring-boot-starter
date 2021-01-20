package org.tbk.bitcoin.jsonrpc.example;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.NetworkInfo;
import com.msgilligan.bitcoinj.json.pojo.UnspentOutput;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class BitcoinJsonrpcClientExampleApplicationConfig {

    @Bean
    @Profile("!test")
    public CommandLineRunner printBitcoinNodeInfo(TaskScheduler scheduler, BitcoinClient bitcoinClient) {
        Runnable task = () -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                BlockChainInfo blockChainInfo = bitcoinClient.getBlockChainInfo();
                log.info("'getBlockChainInfo' after {}", stopwatch);
                log.info("Chain: {}", blockChainInfo.getChain());
                log.info("Difficulty: {}", blockChainInfo.getDifficulty());
                log.info("Blocks: {}", blockChainInfo.getBlocks());
                log.info("BestBlockHash: {}", blockChainInfo.getBestBlockHash());
                log.info("Headers: {}", blockChainInfo.getHeaders());
                log.info("VerificationProgress: {}", blockChainInfo.getVerificationProgress());

                Integer blockCount = bitcoinClient.getBlockCount();
                log.info("'getBlockCount' after {}", stopwatch);
                log.info("Block count: {}", blockCount);

                NetworkInfo networkInfo = bitcoinClient.getNetworkInfo();
                log.info("'getNetworkInfo' after {}", stopwatch);
                log.info("Version: {} ({})", networkInfo.getVersion(), networkInfo.getSubVersion());

                Coin balance = bitcoinClient.getBalance();
                log.info("'getBalance' after {}", stopwatch);
                log.info("Balance: {}", balance);

                Sha256Hash genesisBlockHash = bitcoinClient.getBlockHash(0);
                Block block0 = bitcoinClient.getBlock(genesisBlockHash);
                log.info("'getBlock(0)' after {}", stopwatch);
                log.info("block0 Time: {}", block0.getTime());

                Block blockBest = bitcoinClient.getBlock(blockChainInfo.getBestBlockHash());
                log.info("'getBlock(best)' after {}", stopwatch);
                log.info("blockBest Time: {}", blockBest.getTime());

                final List<UnspentOutput> unspentOutputs = bitcoinClient.listUnspent();
                log.info("'listUnspent' after {}", stopwatch);
                log.info("UnspentOutputs: {}", unspentOutputs);
            } catch (Exception e) {
                log.error("", e);
            } finally {
                stopwatch.stop();
            }
        };

        return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(10L));
    }
}
