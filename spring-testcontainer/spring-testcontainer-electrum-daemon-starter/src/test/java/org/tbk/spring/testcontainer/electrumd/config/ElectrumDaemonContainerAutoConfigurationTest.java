package org.tbk.spring.testcontainer.electrumd.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.config.ElectrumxContainerAutoConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ElectrumDaemonContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumDaemonContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.electrum-daemon.enabled=false"
        ).run(context -> {
            assertThat(context.containsBean("electrumDaemonContainer"), is(false));
            try {
                context.getBean(ElectrumDaemonContainer.class);
                Assertions.fail("Should have thrown exception");
            } catch (NoSuchBeanDefinitionException e) {
                // continue
            }
        });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                BitcoindContainerAutoConfiguration.class,
                ElectrumxContainerAutoConfiguration.class,
                ElectrumDaemonContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrumx.enabled=true",
                "org.tbk.spring.testcontainer.electrumx.rpcuser=myrpcuser",
                "org.tbk.spring.testcontainer.electrumx.rpcpass=correcthorsebatterystaple",
                "org.tbk.spring.testcontainer.electrumx.rpchost=localhost",
                "org.tbk.spring.testcontainer.electrumx.rpcport=18443",
                // ----------------------------------------------
                "org.tbk.spring.testcontainer.electrum-daemon.enabled=true"
        ).run(context -> {
            assertThat(context.containsBean("electrumDaemonContainer"), is(true));
            assertThat(context.getBean(ElectrumDaemonContainer.class), is(notNullValue()));
        });
    }
}
