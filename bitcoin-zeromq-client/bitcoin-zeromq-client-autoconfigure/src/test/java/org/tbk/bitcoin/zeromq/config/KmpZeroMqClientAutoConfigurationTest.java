package org.tbk.bitcoin.zeromq.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.zeromq.kmp.KmpBlockPublisherFactory;
import org.tbk.bitcoin.zeromq.kmp.KmpTransactionPublisherFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KmpZeroMqClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                        BitcoinZeroMqClientAutoConfiguration.class,
                        KmpZeroMqClientAutoConfiguration.class
                )
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.network=mainnet",
                        "org.tbk.bitcoin.zeromq.zmqpubrawblock=tcp://localhost:28332",
                        "org.tbk.bitcoin.zeromq.zmqpubrawtx=tcp://localhost:28333",
                        "org.tbk.bitcoin.zeromq.zmqpubhashblock=tcp://localhost:28334",
                        "org.tbk.bitcoin.zeromq.zmqpubhashtx=tcp://localhost:28335"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinKmpBlockPublisherFactory"), is(true));
                    assertThat(context.getBean(KmpBlockPublisherFactory.class), is(notNullValue()));
                    assertThat(context.containsBean("bitcoinKmpBlockPublishService"), is(true));

                    assertThat(context.containsBean("bitcoinKmpTransactionPublisherFactory"), is(true));
                    assertThat(context.getBean(KmpTransactionPublisherFactory.class), is(notNullValue()));
                    assertThat(context.containsBean("bitcoinKmpTransactionPublishService"), is(true));
                });
    }

    @Test
    void noBeansAreCreated1() {
        this.contextRunner.withUserConfiguration(
                        BitcoinZeroMqClientAutoConfiguration.class,
                        KmpZeroMqClientAutoConfiguration.class
                )
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinKmpBlockPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(KmpBlockPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinKmpBlockPublishService"), is(false));

                    assertThat(context.containsBean("bitcoinKmpTransactionPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(KmpTransactionPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinKmpTransactionPublishService"), is(false));
                });
    }

    @Test
    void noBeansAreCreated2() {
        this.contextRunner.withUserConfiguration(
                        BitcoinZeroMqClientAutoConfiguration.class,
                        KmpZeroMqClientAutoConfiguration.class
                )
                .withPropertyValues(
                        "org.tbk.bitcoin.zeromq.bitcoin-kmp.enabled=false",
                        "org.tbk.bitcoin.zeromq.network=mainnet",
                        "org.tbk.bitcoin.zeromq.zmqpubrawblock=tcp://localhost:28332",
                        "org.tbk.bitcoin.zeromq.zmqpubrawtx=tcp://localhost:28333",
                        "org.tbk.bitcoin.zeromq.zmqpubhashblock=tcp://localhost:28334",
                        "org.tbk.bitcoin.zeromq.zmqpubhashtx=tcp://localhost:28335"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinKmpBlockPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(KmpBlockPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinKmpBlockPublishService"), is(false));

                    assertThat(context.containsBean("bitcoinKmpTransactionPublisherFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(KmpTransactionPublisherFactory.class));
                    assertThat(context.containsBean("bitcoinKmpTransactionPublishService"), is(false));
                });
    }
}
