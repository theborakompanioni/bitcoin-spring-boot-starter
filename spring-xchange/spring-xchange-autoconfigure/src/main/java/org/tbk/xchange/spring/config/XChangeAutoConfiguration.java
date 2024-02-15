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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(Exchange.class)
@EnableConfigurationProperties(XChangeAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.xchange.enabled", havingValue = "true", matchIfMissing = true)
public class XChangeAutoConfiguration {

    private final XChangeAutoConfigProperties properties;

    public XChangeAutoConfiguration(XChangeAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static BeanFactoryPostProcessor xChangeExchangeBeanFactoryPostProcessor() {
        return beanFactory -> {
            beanFactory.addBeanPostProcessor(new XChangeExchangeCreatingBeanPostProcessor(beanFactory));
        };
    }

    public static class XChangeExchangeCreatingBeanPostProcessor implements BeanPostProcessor {

        private final ConfigurableListableBeanFactory beanFactory;

        private final AtomicBoolean shouldInitializeNow = new AtomicBoolean(false);

        public XChangeExchangeCreatingBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = requireNonNull(beanFactory);
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (shouldInitializeNow.getAndSet(false) && !(bean instanceof XChangeAutoConfigProperties)) {
                try {
                    // this will trigger creating the settings bean from the properties file.
                    beanFactory.getBean(XChangeAutoConfigProperties.class);
                } catch (BeansException e) {
                    throw new RuntimeException("Error while creating exchange beans", e);
                }
            }

            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ConfigurationPropertiesBindingPostProcessor) {
                // trigger early creation of exchanges!
                // this is a hack to make them injectable in other beans.
                // otherwise users must have taken them from the application context - which is not a nice
                // good developer experience. Since the classes won't need any beans themselves
                // this should be okay. Maybe it is a bit expensive to create them always so shortly
                // after application startup - because there might be initial http requests for some
                // exchange implementations - but since the user defined it via a properties file,
                // this is probably what is wanted in the first place.
                shouldInitializeNow.set(true);
            }

            if (bean instanceof XChangeAutoConfigProperties xChangeProperties) {
                // As registering beans should be done as early as possible, it might happen that
                // the properties bean has not been processed yet. Trigger it manually, in case it did not happen.
                ConfigurationPropertiesBindingPostProcessor propertiesBindingPostProcessor = beanFactory.getBean(ConfigurationPropertiesBindingPostProcessor.class);
                propertiesBindingPostProcessor.postProcessBeforeInitialization(bean, beanName);

                if (xChangeProperties.isEnabled()) {
                    registerExchangeBeans(xChangeProperties);
                }
            }

            return bean;
        }

        private void registerExchangeBeans(XChangeAutoConfigProperties xChangeProperties) {
            xChangeProperties.getSpecifications().forEach((name, exchangeSpecificationProperties) -> {
                if (beanFactory.containsBean(name)) {
                    log.warn("Skip creating bean '{}' - factory already contains a bean with the same name", name);
                } else {
                    ExchangeSpecification exchangeSpecification = createExchangeSpecification(exchangeSpecificationProperties);
                    Exchange exchange = exchangeSpecificationProperties.getExchangeClassOrThrow()
                            .cast(ExchangeFactory.INSTANCE.createExchange(exchangeSpecification));

                    beanFactory.autowireBean(exchange);
                    beanFactory.initializeBean(exchange, name);
                    beanFactory.registerSingleton(name, exchange);
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
            Optional.ofNullable(properties.getHost()).ifPresent(exchangeSpecification::setHost);
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
