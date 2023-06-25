package org.tbk.bitcoin.exchange.example.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class SwaggerUiWebMvcConfigurer implements WebMvcConfigurer {

    public static final String APP_HANDLER_PATH = "/swagger-ui";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String targetPath = "/swagger-ui/index.html";
        registry.addRedirectViewController(APP_HANDLER_PATH, targetPath);
        registry.addRedirectViewController(APP_HANDLER_PATH + "/", targetPath);
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .version(this.getClass().getPackage().getImplementationVersion())
                        .title("Bitcoin Exchange Rate API")
                        .description("A Bitcoin Exchange Rate API built with bitcoin-spring-boot-starter.")
                        .termsOfService("https://github.com/theborakompanioni/bitcoin-spring-boot-starter")
                        .contact(new Contact()
                                .name("tbk")
                                .url("")
                                .email(""))
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("https://github.com/theborakompanioni/bitcoin-spring-boot-starter/blob/master/LICENSE")
                        )
                );
    }
}