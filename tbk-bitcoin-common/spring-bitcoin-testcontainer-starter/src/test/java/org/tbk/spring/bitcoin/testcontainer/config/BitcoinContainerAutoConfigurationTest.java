package org.tbk.spring.bitcoin.testcontainer.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.GenericContainer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BitcoinContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.bitcoin.testcontainer.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(true));
                    assertThat(context.getBean(GenericContainer.class), is(notNullValue()));
                });
    }


    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinContainerAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.spring.bitcoin.testcontainer.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinContainer"), is(false));
                    try {
                        context.getBean(GenericContainer.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }
}
