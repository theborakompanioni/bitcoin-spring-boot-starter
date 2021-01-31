package org.tbk.bitcoin.exchange.example.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class SwaggerUiWebMvcConfigurer implements WebMvcConfigurer {
    private final String baseUrl;

    public SwaggerUiWebMvcConfigurer(@Value("${springfox.documentation.swagger-ui.base-url:}") String baseUrl) {
        this.baseUrl = StringUtils.trimTrailingCharacter(baseUrl, '/');
        ;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(baseUrl + "/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(baseUrl + "/swagger-ui/", baseUrl + "/swagger-ui/index.html");
    }
}