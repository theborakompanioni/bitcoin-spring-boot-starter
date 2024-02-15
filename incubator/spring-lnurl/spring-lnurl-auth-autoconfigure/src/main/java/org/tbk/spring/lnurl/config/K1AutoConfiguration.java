package org.tbk.spring.lnurl.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.tbk.lnurl.auth.*;

@AutoConfiguration
@ConditionalOnMissingBean(K1Manager.class)
public class K1AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(K1Factory.class)
    SimpleK1Factory k1Factory() {
        return new SimpleK1Factory();
    }

    @Bean
    @ConditionalOnMissingBean(K1Cache.class)
    InMemoryK1Cache k1Cache() {
        return new InMemoryK1Cache();
    }

    @Bean
    SimpleK1Manager k1Manager(K1Factory k1Factory, K1Cache k1Cache) {
        return new SimpleK1Manager(k1Factory, k1Cache);
    }
}
