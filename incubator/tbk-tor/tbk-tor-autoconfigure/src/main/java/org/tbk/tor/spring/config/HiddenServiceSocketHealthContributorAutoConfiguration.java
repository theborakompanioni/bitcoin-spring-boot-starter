package org.tbk.tor.spring.config;

import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.tor.spring.health.HiddenServiceHealthIndicator;
import org.tbk.tor.spring.health.HiddenServiceSocketHealthIndicator;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CompositeHealthContributorConfiguration.class)
@ConditionalOnEnabledHealthIndicator("hiddenServiceSocket")
@AutoConfigureAfter(TorAutoConfiguration.class)
public class HiddenServiceSocketHealthContributorAutoConfiguration extends
        CompositeHealthContributorConfiguration<HiddenServiceSocketHealthIndicator, HiddenServiceSocket> {

    @Bean
    @ConditionalOnBean(HiddenServiceSocket.class)
    @ConditionalOnMissingBean(name = {"hiddenServiceSocketHealthIndicator", "hiddenServiceSocketHealthContributor"})
    public HealthContributor hiddenServiceSocketHealthContributor(Map<String, HiddenServiceSocket> hiddenServiceSockets) {
        return createContributor(hiddenServiceSockets);
    }

}

