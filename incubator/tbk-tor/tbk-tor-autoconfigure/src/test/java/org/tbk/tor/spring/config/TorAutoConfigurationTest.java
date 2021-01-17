package org.tbk.tor.spring.config;

import com.google.common.collect.ImmutableList;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.Torrc;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TorAutoConfigurationTest {
    private static final List<String> beanNames = ImmutableList.<String>builder()
            .add("nativeTor")
            .add("nativeTorFactory")
            .add("torHttpClient")
            .add("torInfoContributor")
            .build();

    // these beans will be created when "autoPublishEnabled" is true
    private static final List<String> autoPublishEnabledBeanNames = ImmutableList.<String>builder()
            .add("applicationHiddenServiceDefinition")
            .add("torrcWithHiddenServiceDefinitions")
            .add("hiddenServiceInfoContributor")
            .build();


    private static final List<String> autoPublishDisabledBeanNames = ImmutableList.<String>builder()
            .add("torrc")
            .build();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner();

    @Test
    public void noBeansCreated() {
        this.contextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .withPropertyValues("org.tbk.tor.enabled=false")
                .run(context -> {
                    beanNames.forEach(name -> {
                        boolean beanWithNameIsAvailable = context.containsBean(name);
                        assertThat(beanWithNameIsAvailable, is(false));
                    });

                    Map<String, Tor> torBeans = context.getBeansOfType(Tor.class);
                    assertThat(torBeans.values(), is(empty()));

                    Map<String, Torrc> torrcBeans = context.getBeansOfType(Torrc.class);
                    assertThat(torrcBeans.values(), is(empty()));

                    Map<String, HiddenServiceDefinition> hiddenServiceBeans = context.getBeansOfType(HiddenServiceDefinition.class);
                    assertThat(hiddenServiceBeans.values(), is(empty()));

                    Map<String, HiddenServiceSocket> hiddenServiceSocketBeans = context.getBeansOfType(HiddenServiceSocket.class);
                    assertThat(hiddenServiceSocketBeans.values(), is(empty()));
                });
    }

    @Test
    public void torBeansCreated() {
        this.contextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .run(context -> {
                    beanNames.forEach(name -> {
                        boolean beanWithNameIsAvailable = context.containsBean(name);
                        assertThat(beanWithNameIsAvailable, is(true));
                    });

                    Map<String, Tor> torBeans = context.getBeansOfType(Tor.class);
                    assertThat(torBeans.values(), hasSize(1));

                    Map<String, Torrc> torrcBeans = context.getBeansOfType(Torrc.class);
                    assertThat(torrcBeans.values(), hasSize(1));

                    Map<String, HiddenServiceDefinition> hiddenServiceBeans = context.getBeansOfType(HiddenServiceDefinition.class);
                    assertThat(hiddenServiceBeans.values(), is(empty()));

                    Map<String, HiddenServiceSocket> hiddenServiceSocketBeans = context.getBeansOfType(HiddenServiceSocket.class);
                    assertThat(hiddenServiceSocketBeans.values(), is(empty()));
                });
    }

    @Test
    public void torBeansCreatedInWebContext() {
        this.webContextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .withBean(ServerProperties.class, () -> {
                    // fake a running webserver for the hidden service to bind to
                    ServerProperties serverProperties = new ServerProperties();
                    serverProperties.setPort(11337);
                    return serverProperties;
                })
                .run(context -> {
                    autoPublishEnabledBeanNames.forEach(name -> {
                        boolean beanWithNameIsAvailable = context.containsBean(name);
                        assertThat(beanWithNameIsAvailable, is(true));
                    });

                    Map<String, HiddenServiceDefinition> hiddenServiceBeans = context.getBeansOfType(HiddenServiceDefinition.class);
                    assertThat(hiddenServiceBeans.values(), hasSize(1));
                });
    }

    @Test
    public void torBeansCreatedInWebContextWithAutoPublishDisabled() {
        this.webContextRunner.withUserConfiguration(TorAutoConfiguration.class)
                .withPropertyValues("org.tbk.tor.auto-publish-enabled=false")
                .run(context -> {
                    autoPublishEnabledBeanNames.forEach(name -> {
                        boolean beanWithNameIsAvailable = context.containsBean(name);
                        assertThat(beanWithNameIsAvailable, is(false));
                    });
                    beanNames.forEach(name -> {
                        boolean beanWithNameIsAvailable = context.containsBean(name);
                        assertThat(beanWithNameIsAvailable, is(true));
                    });

                    Map<String, HiddenServiceDefinition> hiddenServiceBeans = context.getBeansOfType(HiddenServiceDefinition.class);
                    assertThat(hiddenServiceBeans.values(), is(empty()));
                });
    }
}