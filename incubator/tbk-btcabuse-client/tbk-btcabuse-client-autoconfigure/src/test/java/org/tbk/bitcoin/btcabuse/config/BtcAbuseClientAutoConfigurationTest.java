package org.tbk.bitcoin.btcabuse.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.btcabuse.client.BtcAbuseApiClient;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BtcAbuseClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BtcAbuseClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.tool.btcabuse.client.enabled=true",
                        "org.tbk.bitcoin.tool.btcabuse.client.base-url=http://localhost",
                        "org.tbk.bitcoin.tool.btcabuse.client.api-token=123456",
                        "org.tbk.bitcoin.tool.btcabuse.client.user-agent=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("btcAbuseApiClient"), is(true));
                    assertThat(context.getBean(BtcAbuseApiClient.class), is(notNullValue()));
                });
    }

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BtcAbuseClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.tool.btcabuse.client.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("btcAbuseApiClient"), is(false));
                    try {
                        context.getBean(BtcAbuseApiClient.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }
}
