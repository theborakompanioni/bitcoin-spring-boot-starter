package org.tbk.xchange.spring.config;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.ExchangeSpecification.ResilienceSpecification;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XChangeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void noBeansAreAutoCreated() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .run(context -> {
                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), is(empty()));
                });
    }

    @Test
    void beansAreAutoCreatedMinimalProperties() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeClass=org.knowm.xchange.kraken.KrakenExchange",
                        "org.tbk.xchange.specifications.exampleExchange.shouldLoadRemoteMetaData=false"
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
    void beansAreCreatedWithCommonProperties() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=true",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeClass=org.knowm.xchange.kraken.KrakenExchange",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeName=Kraken",
                        "org.tbk.xchange.specifications.exampleExchange.exchangeDescription=any description (can contain whitespaces)",
                        "org.tbk.xchange.specifications.exampleExchange.userName=any-username",
                        "org.tbk.xchange.specifications.exampleExchange.password=any-password",
                        "org.tbk.xchange.specifications.exampleExchange.secretKey=f0000000",
                        "org.tbk.xchange.specifications.exampleExchange.apiKey=any-api-key",
                        "org.tbk.xchange.specifications.exampleExchange.sslUri=https://example.com:8443/exchange",
                        "org.tbk.xchange.specifications.exampleExchange.plainTextUri=http://example.com:8080/exchange",
                        "org.tbk.xchange.specifications.exampleExchange.host=example.com",
                        "org.tbk.xchange.specifications.exampleExchange.port=8080",
                        "org.tbk.xchange.specifications.exampleExchange.proxyHost=proxy.example.com",
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

                    Exchange exchange = requireNonNull(beans.get("exampleExchange"));
                    assertThat(exchange, is(instanceOf(KrakenExchange.class)));

                    ExchangeSpecification spec = exchange.getExchangeSpecification();

                    assertThat(spec.getExchangeName(), is("Kraken"));
                    assertThat(spec.getExchangeDescription(), is("any description (can contain whitespaces)"));
                    assertThat(spec.getUserName(), is("any-username"));
                    assertThat(spec.getPassword(), is("any-password"));
                    assertThat(spec.getSecretKey(), is("f0000000"));
                    assertThat(spec.getApiKey(), is("any-api-key"));
                    assertThat(spec.getSslUri(), is("https://example.com:8443/exchange"));
                    assertThat(spec.getPlainTextUri(), is("http://example.com:8080/exchange"));
                    assertThat(spec.getHost(), is("example.com"));
                    assertThat(spec.getPort(), is(8080));
                    assertThat(spec.getProxyHost(), is("proxy.example.com"));
                    assertThat(spec.getProxyPort(), is(9075));
                    assertThat(spec.getHttpConnTimeout(), is(42));
                    assertThat(spec.getHttpReadTimeout(), is(1337));
                    assertThat(spec.isShouldLoadRemoteMetaData(), is(false));

                    ResilienceSpecification resilience = spec.getResilience();
                    assertThat(resilience.isRetryEnabled(), is(true));
                    assertThat(resilience.isRateLimiterEnabled(), is(true));

                    Object useSandbox = spec.getExchangeSpecificParametersItem("Use_Sandbox");
                    assertThat(useSandbox, is(instanceOf(Boolean.class)));
                    assertThat(useSandbox, is(Boolean.TRUE));
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(XChangeAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.xchange.enabled=false"
                )
                .run(context -> {
                    Map<String, Exchange> beans = context.getBeansOfType(Exchange.class);
                    assertThat(beans.values(), is(empty()));

                    assertThat(context.containsBean("krakenExchange"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(KrakenExchange.class));
                });
    }


    @Test
    void propertiesAreParsedSuccessfully() {
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
