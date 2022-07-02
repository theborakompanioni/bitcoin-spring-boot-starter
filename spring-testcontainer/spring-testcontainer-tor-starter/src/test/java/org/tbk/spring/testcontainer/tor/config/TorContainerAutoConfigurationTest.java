package org.tbk.spring.testcontainer.tor.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.spring.testcontainer.tor.HiddenServiceHostnameResolver;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(TorContainer.class));
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

    @Test
    public void onionAddressesAreGenerated() {
        String serviceName = RandomStringUtils.randomAlphabetic(10);

        this.contextRunner.withUserConfiguration(
                TorContainerAutoConfiguration.class
        ).withPropertyValues(
                "org.tbk.spring.testcontainer.tor.enabled=true",
                "org.tbk.spring.testcontainer.tor.hidden-services." + serviceName + ".virtual-port=80",
                "org.tbk.spring.testcontainer.tor.hidden-services." + serviceName + ".host-port=8080"
        ).run(context -> {
            assertThat(context.containsBean("torContainer"), is(true));
            assertThat(context.getBean(TorContainer.class), is(notNullValue()));

            assertThat(context.containsBean("hiddenServiceHostnameResolver"), is(true));
            assertThat(context.getBean(HiddenServiceHostnameResolver.class), is(notNullValue()));

            HiddenServiceHostnameResolver hostnameResolver = context.getBean(HiddenServiceHostnameResolver.class);
            String onionAddress = hostnameResolver.findHiddenServiceUrl(serviceName)
                    .orElseThrow(() -> new IllegalStateException("Cannot find onion address"));

            assertThat("onion address can be found", onionAddress, endsWith(".onion"));
        });
    }
}
