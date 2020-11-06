package org.tbk.bitcoin.jsonrpc.config;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.jsonrpc.BitcoinClientFactory;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BitcoinJsonRpcClientAutoConfigurationTest {

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
                    assertThat(context.containsBean("bitcoinClientFactory"), is(true));
                    assertThat(context.getBean(BitcoinClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinClient"), is(true));
                    assertThat(context.getBean(BitcoinClient.class), is(notNullValue()));
                });
    }


    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinClientFactory"), is(false));
                    try {
                        context.getBean(BitcoinClientFactory.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }

                    assertThat(context.containsBean("bitcoinClient"), is(false));
                    try {
                        context.getBean(BitcoinClient.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }


    @Test
    public void onlyFactoryIsCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinClientFactory"), is(true));
                    assertThat(context.getBean(BitcoinClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinClient"), is(false));
                    try {
                        context.getBean(BitcoinClient.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }
}
