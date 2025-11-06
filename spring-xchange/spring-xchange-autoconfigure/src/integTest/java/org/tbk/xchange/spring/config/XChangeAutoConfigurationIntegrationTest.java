package org.tbk.xchange.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class XChangeAutoConfigurationIntegrationTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        AtomicReference<Exchange> aBeanInjectedWithDynamicallyCreatedExchangeBean(BitstampExchange bitstampExchange) {
            requireNonNull(bitstampExchange);
            return new AtomicReference<>(bitstampExchange);
        }

        @Bean
        AtomicReference<Exchange> anotherBeanInjectedWithDynamicallyCreatedExchangeBean(BitstampExchange bitstampExchange) {
            requireNonNull(bitstampExchange);
            return new AtomicReference<>(bitstampExchange);
        }
    }

    @Autowired(required = false)
    private AtomicReference<Exchange> aBeanInjectedWithDynamicallyCreatedExchangeBean;

    @Autowired(required = false)
    private AtomicReference<Exchange> anotherBeanInjectedWithDynamicallyCreatedExchangeBean;

    /**
     * this should just fail if the exchange could not be autowired.
     * creating beans dynamically is not that straightforward, so we have to make
     * sure it's still works when we make substantial changes to the code.
     */
    @Test
    void itShouldBePossibleToInjectDynamicallyCreatedExchangeBeans() {
        assertThat(aBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));

        Exchange autowiredExchange = aBeanInjectedWithDynamicallyCreatedExchangeBean.get();
        assertThat(autowiredExchange, is(notNullValue()));
        assertThat(autowiredExchange, is(instanceOf(BitstampExchange.class)));

        ExchangeSpecification exchangeSpecification = autowiredExchange.getExchangeSpecification();

        Boolean sandboxEnabled = (Boolean) exchangeSpecification
                .getExchangeSpecificParametersItem("Use_Sandbox");
        assertThat(sandboxEnabled, is(Boolean.TRUE));

        boolean shouldLoadRemoteMetaData = exchangeSpecification
                .isShouldLoadRemoteMetaData();
        assertThat(shouldLoadRemoteMetaData, is(false));
    }

    @Test
    void itShouldVerifyThatDynamicallyCreatedExchangeBeansAreSingletons() {
        assertThat(aBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));
        assertThat(anotherBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));

        Exchange firstAutowiredExchange = aBeanInjectedWithDynamicallyCreatedExchangeBean.get();
        Exchange secondAutowiredExchange = anotherBeanInjectedWithDynamicallyCreatedExchangeBean.get();

        assertThat(firstAutowiredExchange, is(secondAutowiredExchange));
    }
}
