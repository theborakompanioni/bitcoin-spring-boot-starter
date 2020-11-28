package org.tbk.spring.testcontainer.lnd.config;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerAutoConfiguration;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LndContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoinContainerAutoConfiguration.class,
                LndContainerAutoConfiguration.class
        )
                .withPropertyValues(
                        "org.tbk.spring.lnd.testcontainer.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndContainer"), is(false));
                    try {
                        context.getBean(LndContainer.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoinContainerAutoConfiguration.class,
                LndContainerAutoConfiguration.class
        )
                .withPropertyValues(
                        "org.tbk.spring.bitcoin.testcontainer.enabled=true",
                        "org.tbk.spring.bitcoin.testcontainer.rpcuser=myrpcuser",
                        "org.tbk.spring.bitcoin.testcontainer.rpcpassword=correcthorsebatterystaple",
                        "org.tbk.spring.bitcoin.testcontainer.exposed-ports=28332, 28333",
                        "org.tbk.spring.bitcoin.testcontainer.commands=" +
                                "-zmqpubrawblock=tcp://0.0.0.0:28332, " +
                                "-zmqpubrawtx=tcp://0.0.0.0:28333"
                        ,
                        // ----------------------------------------------
                        "org.tbk.spring.lnd.testcontainer.enabled=true",
                        "org.tbk.spring.lnd.testcontainer.commands=" +
                                "--bitcoind.rpcuser=myrpcuser, " +
                                "--bitcoind.rpcpass=correcthorsebatterystaple"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndContainer"), is(true));
                    assertThat(context.getBean(LndContainer.class), is(notNullValue()));
                });
    }

}
