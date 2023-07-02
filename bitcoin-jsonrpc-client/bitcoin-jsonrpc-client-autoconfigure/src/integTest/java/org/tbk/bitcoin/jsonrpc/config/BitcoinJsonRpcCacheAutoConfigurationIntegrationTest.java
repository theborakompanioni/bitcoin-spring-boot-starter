package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.cache.CacheBuilderSpec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.tbk.bitcoin.jsonrpc.cache.*;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcCacheAutoConfigProperties.CacheBuilderSpecOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "org.tbk.bitcoin.jsonrpc.network=regtest",
        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
        "org.tbk.bitcoin.jsonrpc.rpcpassword=test",
        "org.tbk.bitcoin.jsonrpc-cache.enabled=true",
        "org.tbk.bitcoin.jsonrpc-cache.transaction.enabled=true",
        "org.tbk.bitcoin.jsonrpc-cache.transaction.specification=maximumSize=1",
        "org.tbk.bitcoin.jsonrpc-cache.raw-transaction-info.enabled=true",
        "org.tbk.bitcoin.jsonrpc-cache.raw-transaction-info.specification=maximumSize=2,expireAfterAccess=30m",
        "org.tbk.bitcoin.jsonrpc-cache.block.enabled=false",
        "org.tbk.bitcoin.jsonrpc-cache.block.specification=maximumSize=3",
        "org.tbk.bitcoin.jsonrpc-cache.block-info.enabled=false"
})
class BitcoinJsonRpcCacheAutoConfigurationIntegrationTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinJsonRpcCacheTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinJsonRpcCacheTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void propertiesAreApplied() {
        BitcoinJsonRpcCacheAutoConfigProperties properties = context.getBean(BitcoinJsonRpcCacheAutoConfigProperties.class);
        assertThat(properties, is(notNullValue()));

        CacheBuilderSpecOption transactionOption = properties.getTransaction();
        assertThat(transactionOption.isEnabled(), is(true));
        assertThat(transactionOption.getCacheBuilderSpec().toParsableString(), is("maximumSize=1"));

        CacheBuilderSpecOption rawTransactionInfoOption = properties.getRawTransactionInfo();
        assertThat(rawTransactionInfoOption.isEnabled(), is(true));
        assertThat(rawTransactionInfoOption.getCacheBuilderSpec(), is(CacheBuilderSpec.parse("expireAfterAccess=30m,maximumSize=2")));

        CacheBuilderSpecOption blockOption = properties.getBlock();
        assertThat(blockOption.isEnabled(), is(false));
        assertThat(blockOption.getCacheBuilderSpec(), is(CacheBuilderSpec.disableCaching()));

        CacheBuilderSpecOption blockInfoOption = properties.getBlockInfo();
        assertThat(blockInfoOption.isEnabled(), is(false));
        assertThat(blockInfoOption.getCacheBuilderSpec().toParsableString(), is("maximumSize=0"));
    }

    @Test
    void beansAreCreated() {
        assertThat(context.containsBean("bitcoinJsonRpcTransactionCache"), is(true));
        assertThat(context.getBean(TransactionCache.class), is(notNullValue()));

        assertThat(context.containsBean("bitcoinJsonRpcRawTransactionInfoCache"), is(true));
        assertThat(context.getBean(RawTransactionInfoCache.class), is(notNullValue()));

        assertThat(context.containsBean("bitcoinJsonRpcBlockCache"), is(true));
        assertThat(context.getBean(BlockCache.class), is(notNullValue()));

        assertThat(context.containsBean("bitcoinJsonRpcBlockInfoCache"), is(true));
        assertThat(context.getBean(BlockInfoCache.class), is(notNullValue()));

        assertThat(context.containsBean("bitcoinJsonRpcCacheFacade"), is(true));
        assertThat(context.getBean(CacheFacade.class), is(notNullValue()));
    }
}
