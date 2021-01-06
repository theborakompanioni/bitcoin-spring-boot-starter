package org.tbk.spring.testcontainer.btcrpcexplorer.config;

import com.google.common.base.Throwables;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.btcrpcexplorer.BtcRpcExplorerContainer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BtcRpcExplorerContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BtcRpcExplorerContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.btcrpcexplorer.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("btcRpcExplorerContainer"), is(false));

                    try {
                        context.getBean(BtcRpcExplorerContainer.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BtcRpcExplorerContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.btcrpcexplorer.enabled=true",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcpass=password",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpchost=localhost",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcport=1234",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.electrumx.rpchost=localhost",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.electrumx.tcpport=1235"
                )
                .run(context -> {
                    assertThat(context.containsBean("btcRpcExplorerContainer"), is(true));
                    assertThat(context.getBean(BtcRpcExplorerContainer.class), is(notNullValue()));
                });
    }

    @Test
    public void throwOnInvalidPropertiesValues() {
        this.contextRunner.withUserConfiguration(BtcRpcExplorerContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.btcrpcexplorer.enabled=true",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcpass=password",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpchost=localhost",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.bitcoind.rpcport=1234",
                        "org.tbk.spring.testcontainer.btcrpcexplorer.electrumx.tcpport=0"
                )
                .run(context -> {
                    try {
                        BtcRpcExplorerContainer<?> ignoredOnPurpose = context.getBean(BtcRpcExplorerContainer.class);

                        Assert.fail("Should have failed to start application context");
                    } catch (Exception e) {

                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                    }
                });
    }

}
