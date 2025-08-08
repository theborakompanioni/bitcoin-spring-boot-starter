package org.tbk.electrum.example.shell;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Slf4j
@Configuration(proxyBeanMethods = false)
class ElectrumShellExampleApplicationConfig {

    @Bean
    PromptProvider promptProvider() {
        return () -> new AttributedString("electrum:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA));
    }

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .addModule(new Jdk8Module())
                .build();
    }
}
