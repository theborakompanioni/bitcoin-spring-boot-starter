package org.tbk.lightning.lnd.jsonrpc.config;

import com.google.common.base.Throwables;
import org.junit.Assert;
import org.junit.Test;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.lightning.lnd.jsonrpc.LndJsonRpcClientFactory;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LndJsonRpcClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(LndJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.jsonrpc.rpchost=localhost",
                        "org.tbk.lightning.lnd.jsonrpc.rpcport=10001",
                        "org.tbk.lightning.lnd.jsonrpc.macaroonFilePath=/dev/null",
                        "org.tbk.lightning.lnd.jsonrpc.certFilePath=src/test/resources/lnd/tls-test.cert"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndJsonRpcClientFactory"), is(true));
                    assertThat(context.getBean(LndJsonRpcClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("lndJsonRpcClient"), is(true));
                    assertThat(context.getBean(AsynchronousLndAPI.class), is(notNullValue()));
                });
    }

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(LndJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.jsonrpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndJsonRpcClientFactory"), is(false));
                    try {
                        context.getBean(LndJsonRpcClientFactory.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }

                    assertThat(context.containsBean("lndJsonRpcClient"), is(false));
                    try {
                        context.getBean(AsynchronousLndAPI.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }

    @Test
    public void onlyFactoryIsCreated() {
        this.contextRunner.withUserConfiguration(LndJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.jsonrpc.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndJsonRpcClientFactory"), is(true));
                    assertThat(context.getBean(LndJsonRpcClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("lndJsonRpcClient"), is(false));
                    try {
                        context.getBean(AsynchronousLndAPI.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }

    @Test
    public void errorIfCertFileIsMissing() {
        this.contextRunner.withUserConfiguration(LndJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.jsonrpc.rpchost=localhost",
                        "org.tbk.lightning.lnd.jsonrpc.rpcport=10001",
                        "org.tbk.lightning.lnd.jsonrpc.macaroonFilePath=/dev/null",
                        "org.tbk.lightning.lnd.jsonrpc.certFilePath=src/test/resources/lnd/tls-test-missing.cert"
                ).run(context -> {
            Throwable startupFailure = context.getStartupFailure();
            assertThat(startupFailure, is(notNullValue()));

            Throwable rootCause = Throwables.getRootCause(startupFailure);
            assertThat(rootCause.getMessage(), is("'certFile' must exist"));
        });
    }

}
