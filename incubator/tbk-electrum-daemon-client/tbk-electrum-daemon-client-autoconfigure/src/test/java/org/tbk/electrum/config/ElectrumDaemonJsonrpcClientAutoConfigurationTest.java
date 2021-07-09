package org.tbk.electrum.config;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.ElectrumClientFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ElectrumDaemonJsonrpcClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(ElectrumDaemonJsonrpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.enabled=true",
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.rpchost=http://localhost",
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.rpcport=7000",
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.rpcuser=test",
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.rpcpassword=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("electrumClientFactory"), is(true));
                    assertThat(context.getBean(ElectrumClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("electrumClient"), is(true));
                    assertThat(context.getBean(ElectrumClient.class), is(notNullValue()));
                });
    }


    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(ElectrumDaemonJsonrpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.electrum-daemon.jsonrpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("electrumClientFactory"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ElectrumClientFactory.class));

                    assertThat(context.containsBean("electrumClient"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ElectrumClient.class));
                });
    }
}
