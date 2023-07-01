package org.tbk.bitcoin.jsonrpc.config;

import lombok.extern.slf4j.Slf4j;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.tbk.bitcoin.jsonrpc.BitcoinJsonRpcClientFactory;

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
class BitcoinJsonRpcClientAutoConfigurationIntegrationTest {

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
        assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(true));
        assertThat(context.getBean(BitcoinJsonRpcClientFactory.class), is(notNullValue()));

        assertThat(context.containsBean("bitcoinJsonRpcClient"), is(true));
        assertThat(context.getBean(BitcoinClient.class), is(notNullValue()));
    }
}
