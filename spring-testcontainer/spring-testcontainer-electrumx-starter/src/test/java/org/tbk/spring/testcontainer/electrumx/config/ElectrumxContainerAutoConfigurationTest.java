package org.tbk.spring.testcontainer.electrumx.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ElectrumxContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumxContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.electrumx.enabled=false"
        ).run(context -> {
            assertThat(context.containsBean("electrumxContainer"), is(false));
            try {
                context.getBean(ElectrumxContainer.class);
                Assert.fail("Should have thrown exception");
            } catch (NoSuchBeanDefinitionException e) {
                // continue
            }
        });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumxContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrumx.enabled=true",
                "org.tbk.spring.testcontainer.electrumx.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.electrumx.rpcpass=correcthorsebatterystaple",
                "org.tbk.spring.testcontainer.electrumx.rpchost=localhost",
                "org.tbk.spring.testcontainer.electrumx.rpcport=18443"
        ).run(context -> {
            assertThat(context.containsBean("electrumxContainer"), is(true));
            assertThat(context.getBean(ElectrumxContainer.class), is(notNullValue()));
        });
    }

}
