package org.tbk.bitcoin.autodca.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;

@Slf4j
@SpringBootApplication
public class BitcoinAutoDcaExampleApplication {
    private static final String[] profiles = new String[]{"development", "local"};

    public static void main(String[] args) {
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(args);

        boolean webapp = arguments.containsOption("webapp");
        if (webapp) {
            startWebApplication(args);
        } else {
            startConsoleApplication(args);
        }
    }

    public static void startConsoleApplication(String[] args) {
        new SpringApplicationBuilder()
                .addCommandLineProperties(false)
                .sources(BitcoinAutoDcaExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles(profiles)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .run(args);
    }

    public static void startWebApplication(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinAutoDcaExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles(profiles)
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }
}
