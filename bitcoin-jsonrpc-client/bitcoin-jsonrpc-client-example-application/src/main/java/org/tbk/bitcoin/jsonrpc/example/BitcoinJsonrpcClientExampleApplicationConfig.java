package org.tbk.bitcoin.jsonrpc.example;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.json.pojo.NetworkInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class BitcoinJsonrpcClientExampleApplicationConfig {

    @Bean
    @Profile("!test")
    CommandLineRunner printBitcoinNodeInfo(TaskScheduler scheduler,
                                           BitcoinClient bitcoinClient,
                                           CacheFacade cacheFacade) {
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

                Sha256Hash genesisBlockHash = bitcoinClient.getBlockHash(0);
                Block block0 = cacheFacade.block().getUnchecked(genesisBlockHash);
                log.info("'getBlock(0)' after {}", stopwatch);
                log.info("block0 Time: {}", block0.getTime());

                Block blockBest = cacheFacade.block().getUnchecked(blockChainInfo.getBestBlockHash());
                log.info("'getBlock(best)' after {}", stopwatch);
                log.info("blockBest Time: {}", blockBest.getTime());
            } catch (IOException e) {
                log.error("", e);
            } finally {
                stopwatch.stop();
            }
        };

        return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(10L));
    }

    @Bean
    @Profile("!test")
    CommandLineRunner printCacheStatsInfo(TaskScheduler scheduler, CacheFacade cacheFacade) {
        Runnable task = () -> {
            log.info("Block cache: {}", cacheFacade.block().stats());
            log.info("Block Info cache: {}", cacheFacade.blockInfo().stats());
            log.info("Transaction cache: {}", cacheFacade.tx().stats());
            log.info("Raw Transaction Info cache: {}", cacheFacade.txInfo().stats());
        };
        return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(60L));
    }

    @Bean
    @Profile("!test")
    CommandLineRunner mineBlocksOnRegtest(TaskScheduler scheduler, BitcoinClient bitcoinClient) {
        if (!RegTestParams.get().equals(bitcoinClient.getNetParams())) {
            return args -> {
                log.debug("Not mining any blocks since client is not connected to regtest.");
            };
        } else {
            Address eaterAddress = Address.fromString(RegTestParams.get(), "bcrt1q0lhr8js5rrhjl7hf6e80ns3pgk5tjswgg9um9t");

            Runnable task = () -> {
                try {
                    List<Sha256Hash> blockHashes = bitcoinClient.generateToAddress(1, eaterAddress);
                    log.info("Mined a new block on regtest: {}", blockHashes);
                } catch (IOException e) {
                    log.error("Error while mining regtest block: {}", e.getMessage());
                }
            };
            return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(10L));
        }
    }
}
