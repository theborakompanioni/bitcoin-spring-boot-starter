package org.tbk.bitcoin.regtest.config;

import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(RegtestMiner.class)
@AutoConfigureAfter({
        BitcoinRegtestAutoConfiguration.class,
        BitcoinRegtestMiningAutoConfiguration.class,
})
public class BitcoinRegtestActionsAutoConfiguration {

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean
    RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
        return new RegtestMinerImpl(bitcoinJsonRpcClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({BitcoinClient.class})
    BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
        return new BitcoinRegtestActions(regtestMiner);
    }
}
