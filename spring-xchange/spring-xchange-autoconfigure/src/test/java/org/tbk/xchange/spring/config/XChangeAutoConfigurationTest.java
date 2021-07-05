package org.tbk.xchange.spring.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class XChangeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeansAreAutoCreated() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .run(context -> {
                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), is(empty()));
                });
    }

    @Test
    public void beansAreAutoCreatedMinimalProperties() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeClass=org.knowm.xchange.kraken.KrakenExchange"
                )
                .run(context -> {
                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), hasSize(1));

                    assertThat(context.containsBean("exampleExchange"), is(true));

                    KrakenExchange exampleExchange = context.getBean("exampleExchange", KrakenExchange.class);
                    assertThat(exampleExchange, is(beans.get("exampleExchange")));
                    assertThat(exampleExchange.getExchangeSpecification().getSslUri(), is("https://api.kraken.com"));
                });
    }

    @Test
    public void beansAreCreatedWithCommonProperties() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeClass=org.knowm.xchange.kraken.KrakenExchange",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeName=Example",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeDescription=any description (can contain whitespaces)",
                        "org.tbk.xchange.specifications.exampleExchange.userName=any-username",
                        "org.tbk.xchange.specifications.exampleExchange.password=any-password",
                        "org.tbk.xchange.specifications.exampleExchange.secretKey=f0000000",
                        "org.tbk.xchange.specifications.exampleExchange.apiKey=any-api-key",
                        "org.tbk.xchange.specifications.exampleExchange.sslUri=https://example.com:8443/exchange",
                        "org.tbk.xchange.specifications.exampleExchange.plainTextUri=http://example.com:8080/exchange",
                        "org.tbk.xchange.specifications.exampleExchange.host=example.com",
                        "org.tbk.xchange.specifications.exampleExchange.port=8080",
                        "org.tbk.xchange.specifications.exampleExchange.proxyHost=example.com",
                        "org.tbk.xchange.specifications.exampleExchange.proxyPort=9075",
                        "org.tbk.xchange.specifications.exampleExchange.httpConnTimeout=42",
                        "org.tbk.xchange.specifications.exampleExchange.httpReadTimeout=1337",
                        "org.tbk.xchange.specifications.exampleExchange.shouldLoadRemoteMetaData=false",
                        "org.tbk.xchange.specifications.exampleExchange.resilience.retryEnabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.resilience.rateLimiterEnabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeSpecificParameters.Use_Sandbox=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("exampleExchange"), is(true));
                    assertThat(context.getBean(KrakenExchange.class), is(notNullValue()));

                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), hasSize(1));

                    Exchange exampleExchange = requireNonNull(beans.get("exampleExchange"));

                    ExchangeSpecification exchangeSpecification = exampleExchange.getExchangeSpecification();
                    Object useSandbox = exchangeSpecification.getExchangeSpecificParametersItem("Use_Sandbox");

                    assertThat(useSandbox, is(instanceOf(Boolean.class)));
                    assertThat(useSandbox, is(Boolean.TRUE));
                });
    }

    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=false"
                )
                .run(context -> {
                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), is(empty()));

                    assertThat(context.containsBean("krakenExchange"), is(false));
                    try {
                        context.getBean(KrakenExchange.class);
                        Assertions.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }


    @Test
    public void propertiesAreParsedSuccessfully() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=true",
                        // first exchange
                        "org.tbk.xchange.specifications.firstTestExchange.exchangeClass=org.knowm.xchange.kraken.KrakenExchange",
                        "org.tbk.xchange.specifications.firstTestExchange.exchangeName=Kraken",
                        "org.tbk.xchange.specifications.firstTestExchange.exchangeDescription=Custom Kraken Exchange",
                        "org.tbk.xchange.specifications.firstTestExchange.userName=any",
                        "org.tbk.xchange.specifications.firstTestExchange.password=any",
                        "org.tbk.xchange.specifications.firstTestExchange.secretKey=any",
                        "org.tbk.xchange.specifications.firstTestExchange.apiKey=any",
                        "org.tbk.xchange.specifications.firstTestExchange.shouldLoadRemoteMetaData=false",
                        "org.tbk.xchange.specifications.firstTestExchange.resilience.retryEnabled=true",
                        "org.tbk.xchange.specifications.firstTestExchange.resilience.rateLimiterEnabled=false",
                        // second exchange
                        "org.tbk.xchange.specifications.secondTestExchange.exchangeClass=org.knowm.xchange.bitstamp.BitstampExchange",
                        "org.tbk.xchange.specifications.secondTestExchange.exchangeName=Bitstamp",
                        "org.tbk.xchange.specifications.secondTestExchange.exchangeDescription=Custom Bitstamp Exchange",
                        "org.tbk.xchange.specifications.secondTestExchange.httpConnTimeout=30000",
                        "org.tbk.xchange.specifications.secondTestExchange.httpReadTimeout=100000",
                        "org.tbk.xchange.specifications.secondTestExchange.shouldLoadRemoteMetaData=false",
                        "org.tbk.xchange.specifications.secondTestExchange.resilience.retryEnabled=true",
                        "org.tbk.xchange.specifications.secondTestExchange.resilience.rateLimiterEnabled=true"
                )
                .run(context -> {
                    XChangeAutoConfigProperties bean = context.getBean(XChangeAutoConfigProperties.class);
                    assertThat(bean, is(notNullValue()));

                    assertThat(bean.isEnabled(), is(true));
                    assertThat(bean.getSpecifications().values(), hasSize(2));

                    bean.getSpecifications().forEach((beanName, spec) -> {
                        assertThat("bean with specified name has been created", context.containsBean(beanName), is(true));
                    });

                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), hasSize(2));

                    KrakenExchange firstTestExchange = context.getBean("firstTestExchange", KrakenExchange.class);
                    assertThat(firstTestExchange, is(beans.get("firstTestExchange")));

                    ExchangeSpecification firstTestExchangeSpec = firstTestExchange.getExchangeSpecification();
                    assertThat(firstTestExchangeSpec.getSslUri(), is("https://api.kraken.com"));
                    assertThat(firstTestExchangeSpec.getExchangeDescription(), is("Custom Kraken Exchange"));


                    BitstampExchange secondTestExchange = context.getBean("secondTestExchange", BitstampExchange.class);
                    assertThat(secondTestExchange, is(beans.get("secondTestExchange")));

                    ExchangeSpecification secondTestExchangeSpec = secondTestExchange.getExchangeSpecification();
                    assertThat(secondTestExchangeSpec.getSslUri(), is("https://www.bitstamp.net"));
                    assertThat(secondTestExchangeSpec.getExchangeDescription(), is("Custom Bitstamp Exchange"));
                });
    }
}