package org.tbk.bitcoin.client.example;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableScheduling
public class BitcoinClientApplicationConfig {

    @Bean
    @Profile({"debug"})
    public CommandLineRunner logBeanDefinitionNames(ApplicationContext ctx) {
        return args -> {
            log.info("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                log.info(beanName);
            }

        };
    }

    @Bean
    public CommandLineRunner printBitcoinNodeInfo(TaskScheduler scheduler, BitcoinClient bitcoinClient) {
        Runnable task = () -> {
            try {
                BlockChainInfo blockChainInfo = bitcoinClient.getBlockChainInfo();
                log.info("Chain: {}", blockChainInfo.getChain());
                log.info("Difficulty: {}", blockChainInfo.getDifficulty());
                log.info("Blocks: {}", blockChainInfo.getBlocks());
                log.info("BestBlockHash: {}", blockChainInfo.getBestBlockHash());

                Integer blockCount = bitcoinClient.getBlockCount();
                log.info("Block count: {}", blockCount);

                Coin balance = bitcoinClient.getBalance();
                log.info("Balance: {}", balance);
            } catch (Exception e) {
                log.error("", e);
            }
        };

        return args -> scheduler.scheduleWithFixedDelay(task, Duration.ofSeconds(10L));
    }
}
