package org.tbk.bitcoin.fee.example.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.fee.example.api.FeeCtrl;
import org.tbk.bitcoin.fee.example.internal.api.FeeTableCtrl;
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
                .title("Bitcoin Fee Recommendation API")
                .description("A Bitcoin Fee Recommendation API built with spring-boot-bitcoin-starter.")
                .termsOfServiceUrl("https://github.com/theborakompanioni/spring-boot-bitcoin-starter")
                .contact(new Contact("tbk", "", ""))
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/theborakompanioni/spring-boot-bitcoin-starter/blob/master/LICENSE")
                .version(implementationVersion)
                .build();
    }

    @Bean
    public Docket feeApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("fee")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(FeeCtrl.class.getPackageName()))
                .build();
    }
}
