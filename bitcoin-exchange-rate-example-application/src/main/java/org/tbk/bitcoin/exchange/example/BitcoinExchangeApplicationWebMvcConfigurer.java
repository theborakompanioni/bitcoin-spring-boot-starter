package org.tbk.bitcoin.exchange.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zalando.jackson.datatype.money.MoneyModule;

@EnableWebMvc
@Configuration
public class BitcoinExchangeApplicationWebMvcConfigurer implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/",
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/public/"
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        MoneyModule moneyModule = new MoneyModule()
                .withDefaultFormatting()
                .withQuotedDecimalNumbers();

        return new Jackson2ObjectMapperBuilder()
                .modulesToInstall(moneyModule)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .indentOutput(true)
                .failOnUnknownProperties(false);
    }

}
