package org.tbk.bitcoin.zeromq.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjBlockPublisherFactory;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjTransactionPublisherFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitcoinjZeroMqClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                        BitcoinZeroMqClientAutoConfiguration.class,
                        BitcoinjZeroMqClientAutoConfiguration.class
                )
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.network=mainnet",
                        "org.tbk.bitcoin.zeromq.zmqpubrawblock=tcp://localhost:28332",
                        "org.tbk.bitcoin.zeromq.zmqpubrawtx=tcp://localhost:28333",
                        "org.tbk.bitcoin.zeromq.zmqpubhashblock=tcp://localhost:28334",
                        "org.tbk.bitcoin.zeromq.zmqpubhashtx=tcp://localhost:28335"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinjBlockPublisherFactory"), is(true));
                    assertThat(context.getBean(BitcoinjBlockPublisherFactory.class), is(notNullValue()));
                    assertThat(context.containsBean("bitcoinjBlockPublishService"), is(true));

                    assertThat(context.containsBean("bitcoinjTransactionPublisherFactory"), is(true));
                    assertThat(context.getBean(BitcoinjTransactionPublisherFactory.class), is(notNullValue()));
                    assertThat(context.containsBean("bitcoinjTransactionPublishService"), is(true));
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                        BitcoinZeroMqClientAutoConfiguration.class,
                        BitcoinjZeroMqClientAutoConfiguration.class
                )
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinjBlockPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinjBlockPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinjBlockPublishService"), is(false));

                    assertThat(context.containsBean("bitcoinjTransactionPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinjTransactionPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinjTransactionPublishService"), is(false));
                });
    }
}