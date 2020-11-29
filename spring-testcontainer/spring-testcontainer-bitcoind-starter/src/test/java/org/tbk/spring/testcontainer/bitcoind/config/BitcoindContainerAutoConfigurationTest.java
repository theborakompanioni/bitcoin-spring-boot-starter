package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.base.Throwables;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BitcoindContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(false));
                    try {
                        context.getBean(BitcoindContainer.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(true));
                    assertThat(context.getBean(BitcoindContainer.class), is(notNullValue()));
                });
    }

    @Test
    public void beansWithCustomConfigAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=correcthorsebatterystaple",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(true));
                    assertThat(context.getBean(BitcoindContainer.class), is(notNullValue()));

                    BitcoindContainerProperties properties = context.getBean(BitcoindContainerProperties.class);
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
        this.contextRunner.withUserConfiguration(BitcoindContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.testcontainer.bitcoind.enabled=true",
                        "org.tbk.spring.testcontainer.bitcoind.rpcuser=myrpcuser",
                        "org.tbk.spring.testcontainer.bitcoind.rpcpassword=unsupported password with whitespaces",
                        "org.tbk.spring.testcontainer.bitcoind.commands=-printtoconsole, -debug=1, -logips=1"
                )
                .run(context -> {
                    try {
                        context.start();
                        // triggers creation of container
                        BitcoindContainer<?> ignoredOnPurpose = context.getBean(BitcoindContainer.class);
                        Assert.fail("Should have failed to start application context");
                    } catch (Exception e) {

                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                        assertThat(validationException.getValidationErrors().getAllErrors(), hasSize(1));
                    }
                });
    }

}
