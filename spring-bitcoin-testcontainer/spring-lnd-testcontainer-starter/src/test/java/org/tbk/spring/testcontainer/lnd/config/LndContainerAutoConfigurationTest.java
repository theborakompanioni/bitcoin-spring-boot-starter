package org.tbk.spring.testcontainer.lnd.config;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerAutoConfiguration;
import org.testcontainers.containers.GenericContainer;

import static org.hamcrest.Matchers.*;
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
                        context.getBean(GenericContainer.class);
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
                    assertThat(context.getBeansOfType(GenericContainer.class).values(), hasSize(2));
                });
    }

    /*
    @Test
    public void beansWithCustomConfigAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.bitcoin.testcontainer.enabled=true",
                        "org.tbk.spring.bitcoin.testcontainer.rpcuser=myrpcuser",
                        "org.tbk.spring.bitcoin.testcontainer.rpcpassword=correcthorsebatterystaple",
                        "org.tbk.spring.bitcoin.testcontainer.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(true));
                    assertThat(context.getBean(GenericContainer.class), is(notNullValue()));


                    BitcoinContainerProperties properties = context.getBean(BitcoinContainerProperties.class);
                    assertThat(properties, is(notNullValue()));
                    assertThat(properties.getRpcuser().orElseThrow(), is("myrpcuser"));
                    assertThat(properties.getRpcpassword().orElseThrow(), is("correcthorsebatterystaple"));

                    assertThat(properties.getCommands(), hasSize(3));
                    assertThat(properties.getCommands(), hasItem("-printtoconsole"));
                    assertThat(properties.getCommands(), hasItem("-debug=1"));
                    assertThat(properties.getCommands(), hasItem("-logips=1"));
                });
    }

    @Test
    public void throwOnInvalidPropertiesValues() {
        this.contextRunner.withUserConfiguration(BitcoinContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.bitcoin.testcontainer.enabled=true",
                        "org.tbk.spring.bitcoin.testcontainer.rpcuser=myrpcuser",
                        "org.tbk.spring.bitcoin.testcontainer.rpcpassword=unsupported password with whitespaces",
                        "org.tbk.spring.bitcoin.testcontainer.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        GenericContainer<?> ignoredOnPurpose = context.getBean(GenericContainer.class);
                        Assert.fail("Should have failed to start application context");
                    } catch (Exception e) {

                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                        assertThat(validationException.getValidationErrors().getAllErrors(), hasSize(1));
                    }
                });
    }*/

}
