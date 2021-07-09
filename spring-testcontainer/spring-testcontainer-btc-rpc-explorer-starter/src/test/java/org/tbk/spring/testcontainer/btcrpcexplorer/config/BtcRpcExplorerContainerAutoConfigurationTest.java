package org.tbk.spring.testcontainer.btcrpcexplorer.config;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.btcrpcexplorer.BtcRpcExplorerContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BtcRpcExplorerContainer.class));
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
                    Exception exception = assertThrows(Exception.class, () -> context.getBean(BtcRpcExplorerContainer.class));

                    Throwable rootCause = Throwables.getRootCause(exception);
                    assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                    BindValidationException validationException = (BindValidationException) rootCause;
                    assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                });
    }

}
