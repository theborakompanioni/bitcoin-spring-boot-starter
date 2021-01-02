package org.tbk.spring.testcontainer.tor.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.tor.TorContainer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TorContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(
                TorContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.tor.enabled=false"
        ).run(context -> {
            assertThat(context.containsBean("torContainer"), is(false));
            try {
                context.getBean(TorContainer.class);
                Assert.fail("Should have thrown exception");
            } catch (NoSuchBeanDefinitionException e) {
                // continue
            }
        });
    }

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(
                TorContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.tor.enabled=true"
        ).run(context -> {
            assertThat(context.containsBean("torContainer"), is(true));
            assertThat(context.getBean(TorContainer.class), is(notNullValue()));
        });
    }

}
