package org.tbk.lightning.lnurl.example;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;

@Data
@ConfigurationProperties(
        prefix = "app",
        ignoreUnknownFields = false
)
public class LnurlAuthExampleApplicationProperties {

    private String name;

    private String description;

    private String lnurlAuthBaseUrl;

}
