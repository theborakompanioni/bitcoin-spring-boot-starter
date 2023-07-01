package org.tbk.bitcoin.jsonrpc.config;

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
        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
})
class BitcoinJsonRpcCacheAutoConfigurationIntegrationTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinJsonRpcTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinJsonRpcTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired
    private ApplicationContext context;

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
