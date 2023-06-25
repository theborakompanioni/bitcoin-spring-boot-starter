package org.tbk.bitcoin.regtest.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class BitcoinRegtestExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinRegtestExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }
}
