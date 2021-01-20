package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.tbk.tor.hs.HiddenServiceDefinition;
import org.tbk.tor.spring.config.TorAutoConfigProperties.OnionLocationHeaderProperties;
import org.tbk.tor.spring.filter.OnionLocationHeaderFilter;

import javax.servlet.Filter;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication
@ConditionalOnClass(Filter.class)
@AutoConfigureAfter(TorHiddenServiceAutoConfiguration.class)
public class TorWebFilterAutoConfiguration {

    private final TorAutoConfigProperties properties;

    public TorWebFilterAutoConfiguration(TorAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnBean(HiddenServiceDefinition.class)
    @ConditionalOnProperty(value = "org.tbk.tor.onion-location-header.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<OnionLocationHeaderFilter> onionLocationHeaderFilter(ObjectProvider<HiddenServiceDefinition> hiddenServiceDefinition) {
        OnionLocationHeaderFilter filter = Optional.ofNullable(hiddenServiceDefinition.getIfUnique())
                .map(OnionLocationHeaderFilter::create)
                .orElseGet(OnionLocationHeaderFilter::noop);

        OnionLocationHeaderProperties onionLocationHeaderProperties = properties.getOnionLocationHeader();
        filter.setAllowOnLocalhostWithHttp(onionLocationHeaderProperties.isAllowOnLocalhostHttp());

        var bean = new FilterRegistrationBean<OnionLocationHeaderFilter>();
        bean.setFilter(filter);
        bean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return bean;
    }

}
