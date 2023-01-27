package org.tbk.spring.lnurl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lnurl.auth.*;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(K1Manager.class)
public class K1AutoConfiguration {

    @Bean
    public SimpleK1Manager k1Manager(K1Factory k1Factory, K1Cache k1Cache) {
        return new SimpleK1Manager(k1Factory, k1Cache);
    }

    @Bean
    @ConditionalOnMissingBean(K1Factory.class)
    public SimpleK1Factory k1Factory() {
        return new SimpleK1Factory();
    }

    @Bean
    @ConditionalOnMissingBean(K1Cache.class)
    public InMemoryK1Cache k1Cache() {
        return new InMemoryK1Cache();
    }
}
