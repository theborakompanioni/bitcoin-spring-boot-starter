package org.tbk.xchange.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeClassUtils;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@ConditionalOnClass(Exchange.class)
@EnableConfigurationProperties(XChangeAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.xchange.enabled", havingValue = "true", matchIfMissing = true)
public class XChangeAutoConfiguration {

    private XChangeAutoConfigProperties properties;

    public XChangeAutoConfiguration(XChangeAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    public static BeanFactoryPostProcessor xChangeExchangeBeanFactoryPostProcessor() {
        return beanFactory -> {
            beanFactory.addBeanPostProcessor(new XChangeExchangeCreatingBeanPostProcessor(beanFactory));
        };
    }

    public static class XChangeExchangeCreatingBeanPostProcessor implements BeanPostProcessor {

        private final ConfigurableListableBeanFactory beanFactory;

        public XChangeExchangeCreatingBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = requireNonNull(beanFactory);
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof XChangeAutoConfigProperties) {
                XChangeAutoConfigProperties xChangeAutoConfigProperties = (XChangeAutoConfigProperties) bean;
                registerExchangeBeans(xChangeAutoConfigProperties);
            }

            return bean;
        }

        private void registerExchangeBeans(XChangeAutoConfigProperties xChangeAutoConfigProperties) {
            xChangeAutoConfigProperties.getSpecifications().forEach((name, exchangeSpecificationProperties) -> {
                if (beanFactory.containsBean(name)) {
                    log.warn("Skip creating bean '{}' - factory already contains a bean with the same name", name);
                } else {
                    ExchangeSpecification exchangeSpecification = createExchangeSpecification(exchangeSpecificationProperties);
                    Exchange exchange = exchangeSpecificationProperties.getExchangeClassOrThrow()
                        .cast(ExchangeFactory.INSTANCE.createExchange(exchangeSpecification));

                    beanFactory.registerSingleton(name, exchange);
                    beanFactory.initializeBean(exchange, name);
                }
            });
        }

        private static ExchangeSpecification createExchangeSpecification(ExchangeSpecificationProperties properties) {
            ExchangeClassUtils.exchangeClassForName(properties.getExchangeClass());

            Exchange defaultExchange = ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(properties.getExchangeClassOrThrow());

            ExchangeSpecification exchangeSpecification = defaultExchange.getDefaultExchangeSpecification();

            Optional.ofNullable(properties.getApiKey()).ifPresent(exchangeSpecification::setApiKey);
            Optional.ofNullable(properties.getExchangeDescription()).ifPresent(exchangeSpecification::setExchangeDescription);
            Optional.ofNullable(properties.getExchangeName()).ifPresent(exchangeSpecification::setExchangeName);
            Optional.ofNullable(properties.getApiKey()).ifPresent(exchangeSpecification::setHost);
            Optional.ofNullable(properties.getHttpConnTimeout()).ifPresent(exchangeSpecification::setHttpConnTimeout);
            Optional.ofNullable(properties.getHttpReadTimeout()).ifPresent(exchangeSpecification::setHttpReadTimeout);
            Optional.ofNullable(properties.getMetaDataJsonFileOverride()).ifPresent(exchangeSpecification::setMetaDataJsonFileOverride);
            Optional.ofNullable(properties.getPassword()).ifPresent(exchangeSpecification::setPassword);
            Optional.ofNullable(properties.getPlainTextUri()).ifPresent(exchangeSpecification::setPlainTextUri);
            Optional.ofNullable(properties.getPort()).ifPresent(exchangeSpecification::setPort);
            Optional.ofNullable(properties.getProxyHost()).ifPresent(exchangeSpecification::setProxyHost);
            Optional.ofNullable(properties.getProxyPort()).ifPresent(exchangeSpecification::setProxyPort);
            Optional.ofNullable(properties.getSecretKey()).ifPresent(exchangeSpecification::setSecretKey);
            Optional.ofNullable(properties.getShouldLoadRemoteMetaData()).ifPresent(exchangeSpecification::setShouldLoadRemoteMetaData);
            Optional.ofNullable(properties.getSslUri()).ifPresent(exchangeSpecification::setSslUri);
            Optional.ofNullable(properties.getUserName()).ifPresent(exchangeSpecification::setUserName);

            Optional.ofNullable(properties.getResilience()).ifPresent(it -> {
                Optional.ofNullable(it.getRateLimiterEnabled()).ifPresent(exchangeSpecification.getResilience()::setRateLimiterEnabled);
                Optional.ofNullable(it.getRetryEnabled()).ifPresent(exchangeSpecification.getResilience()::setRetryEnabled);
            });

            properties.getExchangeSpecificParameters().forEach(exchangeSpecification::setExchangeSpecificParametersItem);

            return exchangeSpecification;
        }
    }
}
