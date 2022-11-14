package org.tbk.bitcoin.fee.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.util.JsonFormat;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@EnableWebMvc
@Configuration(proxyBeanMethods = false)
class BitcoinFeeExampleApplicationWebMvcConfigurer implements WebMvcConfigurer {

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

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "index.html");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, protobufJsonFormatHttpMessageConverter(
                protobufJsonParser(),
                protobufJsonPrinter()
        ));

        customizeJacksonMessageConverter(converters);
    }

    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter(JsonFormat.Parser jsonParser,
                                                                                         JsonFormat.Printer jsonPrinter) {
        return new ProtobufJsonFormatHttpMessageConverter(jsonParser, jsonPrinter);
    }

    @Bean
    public JsonFormat.Printer protobufJsonPrinter() {
        return JsonFormat.printer().sortingMapKeys();
    }

    @Bean
    public JsonFormat.Parser protobufJsonParser() {
        return JsonFormat.parser().ignoringUnknownFields();
    }

    /**
     * This is the only way that worked making jackson pretty print json responses.
     *
     * <p>No, beans of {@link Jackson2ObjectMapperBuilder}, {@link MappingJackson2HttpMessageConverter} or
     * {@link Jackson2ObjectMapperBuilderCustomizer} did the job properly (which is very odd).
     * Maybe try again at a later point in time. But this is good for now (2020-10-24).
     */
    private static void customizeJacksonMessageConverter(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(any -> any instanceof MappingJackson2HttpMessageConverter)
                .map(any -> (MappingJackson2HttpMessageConverter) any)
                .forEach(converter -> configureObjectMapper(converter.getObjectMapper()));
    }

    private static void configureObjectMapper(ObjectMapper objectMapper) {
        SimpleModule internalModule = new SimpleModule("AppInternal")
                .addSerializer(new BigDecimalToStringSerializer());


        objectMapper
                .registerModule(internalModule)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    public static final class BigDecimalToStringSerializer extends JsonSerializer<BigDecimal> {

        @Override
        public Class<BigDecimal> handledType() {
            return BigDecimal.class;
        }

        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toPlainString());
        }
    }
}
