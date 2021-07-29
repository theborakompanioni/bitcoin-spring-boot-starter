package org.tbk.lightning.lnurl.example.lnurl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class K1Config {

    @Bean
    public K1Manager k1Manager(K1Factory k1Factory, K1Cache k1Cache) {
        return new SimpleK1Manager(k1Factory, k1Cache);
    }

    @Bean
    public K1Factory k1Factory() {
        return new SimpleK1Factory();
    }

    @Bean
    public K1Cache k1Cache() {
        return new InMemoryK1Cache();
    }
}
