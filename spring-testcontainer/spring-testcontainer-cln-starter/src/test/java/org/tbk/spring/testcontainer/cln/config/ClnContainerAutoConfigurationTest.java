package org.tbk.spring.testcontainer.cln.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.cln.ClnContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClnContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ClnContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.cln.enabled=false"
        ).run(context -> {
            assertThat(context.containsBean("clnContainer"), is(false));
            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ClnContainer.class));
        });
    }

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ClnContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                "org.tbk.spring.testcontainer.bitcoind.exposed-ports=28332, 28333",
                "org.tbk.spring.testcontainer.bitcoind.commands="
                        + "-zmqpubrawblock=tcp://0.0.0.0:28332, "
                        + "-zmqpubrawtx=tcp://0.0.0.0:28333"
                ,
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.cln.enabled=true",
                "org.tbk.spring.testcontainer.cln.commands="
                        + "--bitcoin-rpcuser=myrpcuser, "
                        + "--bitcoin-rpcpassword=correcthorsebatterystaple"
        ).run(context -> {
            assertThat(context.containsBean("clnContainer"), is(true));
            assertThat(context.getBean(ClnContainer.class), is(notNullValue()));
        });
    }
}
