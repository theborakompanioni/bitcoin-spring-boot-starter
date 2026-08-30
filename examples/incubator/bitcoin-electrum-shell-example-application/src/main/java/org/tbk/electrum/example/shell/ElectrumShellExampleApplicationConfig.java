package org.tbk.electrum.example.shell;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Configuration(proxyBeanMethods = false)
class ElectrumShellExampleApplicationConfig {

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }
}
