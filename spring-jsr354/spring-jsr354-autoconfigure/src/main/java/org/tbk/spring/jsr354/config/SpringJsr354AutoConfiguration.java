package org.tbk.spring.jsr354.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.jsr354.SpringContextAwareServiceProvider;

import javax.money.Monetary;
import javax.money.spi.Bootstrap;
import javax.money.spi.ServiceProvider;
import java.util.Optional;
import java.util.ServiceLoader;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpringJsr354AutoConfigProperties.class)
@ConditionalOnClass(SpringContextAwareServiceProvider.class)
@ConditionalOnProperty(value = "org.tbk.spring.jsr354.enabled", havingValue = "true", matchIfMissing = true)
public class SpringJsr354AutoConfiguration {

    private final SpringJsr354AutoConfigProperties properties;

    public SpringJsr354AutoConfiguration(SpringJsr354AutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    SpringContextAwareServiceProvider springApplicationContextServiceProvider() {
        ServiceProvider delegateOrNull = loadDefaultServiceProvider().orElse(null);
        return new SpringContextAwareServiceProvider(10, delegateOrNull);
    }

    @Bean
    InitializingBean jsr354AutoBootstrap(SpringContextAwareServiceProvider provider) {
        return () -> {
            String providerName = provider.getClass().getName();
            if (properties.isAutobootstrap()) {
                log.info("Spring Money Bootstrap: will init {}", providerName);
                Bootstrap.init(provider);
            } else {
                log.info("Spring Money Bootstrap: will not init with {}", providerName);
            }
        };
    }

    private static Optional<ServiceProvider> loadDefaultServiceProvider() {
        try {
            //noinspection LoopStatementThatDoesntLoop
            for (ServiceProvider sp : ServiceLoader.load(ServiceProvider.class, Monetary.class.getClassLoader())) {
                return Optional.of(sp);
            }
        } catch (Exception e) {
            log.warn("No ServiceProvider loaded, using no delegate.");
        }
        return Optional.empty();
    }
}
