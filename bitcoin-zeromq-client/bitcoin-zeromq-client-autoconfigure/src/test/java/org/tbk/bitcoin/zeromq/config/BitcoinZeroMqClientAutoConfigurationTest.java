package org.tbk.bitcoin.zeromq.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitcoinZeroMqClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinZeroMqClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.network=mainnet",
                        "org.tbk.bitcoin.zeromq.zmqpubrawblock=tcp://localhost:28332",
                        "org.tbk.bitcoin.zeromq.zmqpubrawtx=tcp://localhost:28333",
                        "org.tbk.bitcoin.zeromq.zmqpubhashblock=tcp://localhost:28334",
                        "org.tbk.bitcoin.zeromq.zmqpubhashtx=tcp://localhost:28335"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinZmqClientConfig"), is(true));
                    assertThat(context.getBean(BitcoinZmqClientConfig.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinRawBlockZeroMqMessagePublisherFactory"), is(true));
                    assertThat(context.containsBean("bitcoinRawTxZeroMqMessagePublisherFactory"), is(true));
                    assertThat(context.containsBean("bitcoinHashBlockZeroMqMessagePublisherFactory"), is(true));
                    assertThat(context.containsBean("bitcoinHashTxZeroMqMessagePublisherFactory"), is(true));
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinZeroMqClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinZmqClientConfig"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinZmqClientConfig.class));

                    assertThat(context.containsBean("bitcoinRawBlockZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinRawTxZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinHashBlockZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinHashTxZeroMqMessagePublisherFactory"), is(false));
                });
    }

    @Test
    void onlyConfigIsCreated() {
        this.contextRunner.withUserConfiguration(BitcoinZeroMqClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinZmqClientConfig"), is(true));
                    assertThat(context.getBean(BitcoinZmqClientConfig.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinRawBlockZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinRawTxZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinHashBlockZeroMqMessagePublisherFactory"), is(false));
                    assertThat(context.containsBean("bitcoinHashTxZeroMqMessagePublisherFactory"), is(false));
                });
    }
}