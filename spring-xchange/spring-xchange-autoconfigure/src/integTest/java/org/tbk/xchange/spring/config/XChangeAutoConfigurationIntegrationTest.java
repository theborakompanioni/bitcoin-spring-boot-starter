package org.tbk.xchange.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.kraken.KrakenExchange;
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
public class XChangeAutoConfigurationIntegrationTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public AtomicReference<Exchange> aBeanInjectedWithDynamicallyCreatedExchangeBean(KrakenExchange krakenExchange) {
            requireNonNull(krakenExchange);
            return new AtomicReference<>(krakenExchange);
        }

        @Bean
        public AtomicReference<Exchange> anotherBeanInjectedWithDynamicallyCreatedExchangeBean(KrakenExchange krakenExchange) {
            requireNonNull(krakenExchange);
            return new AtomicReference<>(krakenExchange);
        }
    }

    @Autowired(required = false)
    private AtomicReference<Exchange> aBeanInjectedWithDynamicallyCreatedExchangeBean;

    @Autowired(required = false)
    private AtomicReference<Exchange> anotherBeanInjectedWithDynamicallyCreatedExchangeBean;

    /**
     * this should just fail if the exchange could not be autowired.
     * creating beans dynamically is not that straightforward so we have to make
     * sure its still works when we make substantial changes to the code.
     */
    @Test
    public void itShouldBePossibleToInjectDynamicallyCreatedExchangeBeans() {
        assertThat(aBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));

        Exchange autowiredExchange = aBeanInjectedWithDynamicallyCreatedExchangeBean.get();
        assertThat(autowiredExchange, is(notNullValue()));
        assertThat(autowiredExchange, is(instanceOf(KrakenExchange.class)));

        Boolean sandboxEnabled = (Boolean) autowiredExchange.getExchangeSpecification()
                .getExchangeSpecificParametersItem("Use_Sandbox");

        assertThat(sandboxEnabled, is(Boolean.TRUE));
    }


    @Test
    public void itShouldVerifyThatDynamicallyCreatedExchangeBeansAreSingletons() {
        assertThat(aBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));
        assertThat(anotherBeanInjectedWithDynamicallyCreatedExchangeBean, is(notNullValue()));

        Exchange firstAutowiredExchange = aBeanInjectedWithDynamicallyCreatedExchangeBean.get();
        Exchange secondAutowiredExchange = anotherBeanInjectedWithDynamicallyCreatedExchangeBean.get();

        assertThat(firstAutowiredExchange, is(secondAutowiredExchange));
    }
}
