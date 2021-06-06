package org.tbk.bitcoin.fee.example.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.fee.example.api.FeeCtrl;
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
    public SecurityConfiguration swaggerSecurityConfiguration() {
        return SecurityConfigurationBuilder.builder()
                .enableCsrfSupport(true)
                .build();
    }

    @Bean
    public Contact swaggerContact() {
        return new Contact("tbk", "", "");
    }

    @Bean
    public ApiInfo swaggerApiInfo(Contact swaggerContact) {
        return new ApiInfoBuilder()
                .title("Bitcoin Fee Recommendation API")
                .description("A Bitcoin Fee Recommendation API built with bitcoin-spring-boot-starter.")
                .termsOfServiceUrl("https://github.com/theborakompanioni/bitcoin-spring-boot-starter")
                .contact(swaggerContact)
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/LICENSE")
                .version(implementationVersion)
                .build();
    }

    @Bean
    public Docket swaggerFeeApiDocket(ApiInfo swaggerApiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("fee")
                .apiInfo(swaggerApiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage(FeeCtrl.class.getPackageName()))
                .build();
    }
}
