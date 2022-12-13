package org.tbk.bitcoin.jsonrpc.config;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.jsonrpc.BitcoinJsonRpcClientFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitcoinJsonRpcClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.network=mainnet",
                        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
                        "org.tbk.bitcoin.jsonrpc.rpcport=7000",
                        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
                        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(true));
                    assertThat(context.getBean(BitcoinJsonRpcClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinJsonRpcClient"), is(true));
                    assertThat(context.getBean(BitcoinClient.class), is(notNullValue()));
                });
    }


    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinJsonRpcClientFactory.class));

                    assertThat(context.containsBean("bitcoinJsonRpcClient"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinClient.class));
                });
    }


    @Test
    void onlyFactoryIsCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(true));
                    assertThat(context.getBean(BitcoinJsonRpcClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinJsonRpcClient"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinClient.class));
                });
    }
}
