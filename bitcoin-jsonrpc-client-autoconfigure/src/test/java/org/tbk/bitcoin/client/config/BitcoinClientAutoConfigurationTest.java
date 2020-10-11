package org.tbk.bitcoin.client.config;


import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.client.BitcoinClientFactory;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BitcoinClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.enabled=true",
                        "org.tbk.bitcoin.client.enabled=true",
                        "org.tbk.bitcoin.client.network=mainnet",
                        "org.tbk.bitcoin.client.rpchost=http://localhost",
                        "org.tbk.bitcoin.client.rpcport=7000",
                        "org.tbk.bitcoin.client.rpcuser=test",
                        "org.tbk.bitcoin.client.rpcpassword=test"
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
        this.contextRunner.withUserConfiguration(BitcoinClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.enabled=false",
                        "org.tbk.bitcoin.client.enabled=true"
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
        this.contextRunner.withUserConfiguration(BitcoinClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.enabled=true",
                        "org.tbk.bitcoin.client.enabled=false"
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
