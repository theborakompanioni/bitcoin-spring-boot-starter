package org.tbk.bitcoin.exchange.example.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.example.api.currency.CurrencyUnitCtrl;
import org.tbk.bitcoin.exchange.example.api.rate.ExchangeRateCtrl;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration(proxyBeanMethods = false)
@EnableSwagger2
public class SwaggerConfig {
    private static final String implementationVersion = SwaggerConfig.class.getPackage().getImplementationVersion();

    @Bean
    public SwaggerUiWebMvcConfigurer swaggerUiWebMvcConfigurer(@Value("${springfox.documentation.swagger-ui.base-url:}") String baseUrl) {
        return new SwaggerUiWebMvcConfigurer(baseUrl);
    }

    @Bean
    public SecurityConfiguration securityConfiguration() {
        return SecurityConfigurationBuilder.builder()
                .enableCsrfSupport(true)
                .build();
    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Bitcoin Exchange Rate API")
                .description("A Bitcoin Exchange Rate API built with bitcoin-spring-boot-starter.")
                .termsOfServiceUrl("https://github.com/theborakompanioni/bitcoin-spring-boot-starter")
                .contact(new Contact("tbk", "", ""))
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/LICENSE")
                .version(implementationVersion)
                .build();
    }

    @Bean
    public Docket currencyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("currency")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(CurrencyUnitCtrl.class.getPackageName()))
                .build();

    }

    @Bean
    public Docket exchangeApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("exchange")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(ExchangeRateCtrl.class.getPackageName()))
                .build();

    }
}
