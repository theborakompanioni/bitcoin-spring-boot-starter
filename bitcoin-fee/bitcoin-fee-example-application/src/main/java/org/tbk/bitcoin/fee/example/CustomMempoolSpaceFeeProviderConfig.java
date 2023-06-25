package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.ProviderInfo.SimpleProviderInfo;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClient;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.mempoolspace.ProjectedBlocksMempoolspaceFeeProvider;
import org.tbk.bitcoin.tool.fee.mempoolspace.SimpleMempoolspaceFeeProvider;
import org.tbk.bitcoin.tool.fee.mempoolspace.config.MempoolspaceFeeClientAutoConfiguration;

@Slf4j
@Configuration
@EnableAutoConfiguration(exclude = {
        // disable auto-creation of mempool.space fee providers as we want to create multiple custom providers ourselves
        MempoolspaceFeeClientAutoConfiguration.class
})
class CustomMempoolSpaceFeeProviderConfig {

    // ****************************************** mempool.space
    @Bean
    MempoolspaceFeeApiClient mempoolspaceFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl("https://mempool.space", null);
    }

    @Bean
    SimpleMempoolspaceFeeProvider mempoolspaceFeeProvider() {
        return new SimpleMempoolspaceFeeProvider(mempoolspaceFeeApiClient(), SimpleProviderInfo.builder()
                .name("mempool.space-simple")
                .description("Simple fee recommendation")
                .build());
    }

    @Bean
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, SimpleProviderInfo.builder()
                .name("mempool.space")
                .description("Fee recommendation using projected blocks of the mempool")
                .build());
    }

    // ****************************************** mempool.bisq.services
    @Bean
    MempoolspaceFeeApiClient mempoolBisqFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl("https://mempool.bisq.services", null);
    }

    @Bean
    SimpleMempoolspaceFeeProvider mempoolBisqFeeProvider() {
        return new SimpleMempoolspaceFeeProvider(mempoolBisqFeeApiClient(), SimpleProviderInfo.builder()
                .name("mempool.bisq.services-simple")
                .description("Simple fee recommendation")
                .build());
    }

    @Bean
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolBisqFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, SimpleProviderInfo.builder()
                .name("mempool.bisq.services")
                .description("Fee recommendation using projected blocks of the mempool")
                .build());
    }

    // ****************************************** mempool.emzy.de
    @Bean
    MempoolspaceFeeApiClient mempoolEmzyFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl("https://mempool.emzy.de", null);
    }

    @Bean
    SimpleMempoolspaceFeeProvider mempoolEmzyFeeProvider() {
        return new SimpleMempoolspaceFeeProvider(mempoolEmzyFeeApiClient(), SimpleProviderInfo.builder()
                .name("mempool.emzy.de-simple")
                .description("Simple fee recommendation")
                .build());
    }

    @Bean
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolEmzyFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, SimpleProviderInfo.builder()
                .name("mempool.emzy.de")
                .description("Fee recommendation using projected blocks of the mempool")
                .build());
    }

    // ****************************************** mempool.ninja
    @Bean
    MempoolspaceFeeApiClient mempoolNinjaFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl("https://mempool.ninja", null);
    }

    @Bean
    SimpleMempoolspaceFeeProvider mempoolNinjaFeeProvider() {
        return new SimpleMempoolspaceFeeProvider(mempoolNinjaFeeApiClient(), SimpleProviderInfo.builder()
                .name("mempool.ninja-simple")
                .description("Simple fee recommendation")
                .build());
    }

    @Bean
    ProjectedBlocksMempoolspaceFeeProvider projectedBlocksMempoolNinjaFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new ProjectedBlocksMempoolspaceFeeProvider(mempoolspaceFeeApiClient, SimpleProviderInfo.builder()
                .name("mempool.ninja")
                .description("Fee recommendation using projected blocks of the mempool")
                .build());
    }
}
