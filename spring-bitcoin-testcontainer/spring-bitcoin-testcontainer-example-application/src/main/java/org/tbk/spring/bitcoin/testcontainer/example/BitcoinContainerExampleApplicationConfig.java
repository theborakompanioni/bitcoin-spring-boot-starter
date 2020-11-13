package org.tbk.spring.bitcoin.testcontainer.example;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfigProperties;
import org.tbk.spring.bitcoin.testcontainer.example.regtest.PeriodicBitcoinContainerRegtestMiner;
import org.testcontainers.containers.GenericContainer;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@Configuration
public class BitcoinContainerExampleApplicationConfig {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    public RpcConfig bitcoinJsonRpcConfig(NetworkParameters bitcoinNetworkParameters,
                                          BitcoinJsonRpcClientAutoConfigProperties properties,
                                          @Qualifier("bitcoinContainer") GenericContainer<?> bitcoinContainer) {
        URI uri = URI.create(properties.getRpchost() + ":" + bitcoinContainer.getMappedPort(properties.getRpcport()));
        return new RpcConfig(bitcoinNetworkParameters, uri, properties.getRpcuser(), properties.getRpcpassword());
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    public PeriodicBitcoinContainerRegtestMiner periodicBitcoinContainerRegtestMiner(BitcoinClient bitcoinJsonRpcClient) {
        Duration initDelay = Duration.ofSeconds(10);
        Duration durationTillNewBlock = Duration.ofSeconds(10);

        Scheduler scheduler = Scheduler.newFixedDelaySchedule(initDelay.toMillis(), durationTillNewBlock.toMillis(), TimeUnit.MILLISECONDS);

        return new PeriodicBitcoinContainerRegtestMiner(bitcoinJsonRpcClient, scheduler);
    }
}
