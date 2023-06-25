package org.tbk.electrum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class ElectrumClientTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectrumClientTestApplication.class, args);
    }

}
