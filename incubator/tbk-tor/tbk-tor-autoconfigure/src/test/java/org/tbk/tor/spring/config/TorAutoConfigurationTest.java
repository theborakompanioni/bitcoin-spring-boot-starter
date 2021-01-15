package org.tbk.tor.spring.config;

import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void torBeanCreated() {
        this.contextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .run(context -> {
                    Map<String, Tor> torBeans = context.getBeansOfType(Tor.class);
                    assertThat(torBeans.values(), hasSize(1));

                    Map<String, HiddenServiceSocket> hiddenServiceBeans = context.getBeansOfType(HiddenServiceSocket.class);
                    assertThat(hiddenServiceBeans.values(), is(empty()));
                });
    }

    @Test
    public void hiddenServiceNotBeanCreated() {
        this.contextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .run(context -> {
                    Map<String, HiddenServiceSocket> hiddenServiceBeans = context.getBeansOfType(HiddenServiceSocket.class);
                    assertThat(hiddenServiceBeans.values(), is(empty()));
                });
    }
}