package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.io.File;
import java.net.InetAddress;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(TorAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(TorAutoConfiguration.class)
public class ApplicationHiddenServicePublishAutoConfiguration {
    static class OnAutoPublishEnabledAndServerPortSpecified extends AllNestedConditions {

        OnAutoPublishEnabledAndServerPortSpecified() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(name = "org.tbk.tor.auto-publish-enabled", havingValue = "true", matchIfMissing = true)
        static class OnAutoPublishEnabled {
        }

        @ConditionalOnProperty(name = "server.port")
        static class OnServerPortSpecified {
        }
    }

    private final TorAutoConfigProperties properties;

    public ApplicationHiddenServicePublishAutoConfiguration(TorAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Primary
    @Bean("applicationHiddenServiceDefinition")
    @ConditionalOnMissingBean(name = "applicationHiddenServiceDefinition")
    @ConditionalOnWebApplication
    @ConditionalOnBean(ServerProperties.class)
    @Conditional(OnAutoPublishEnabledAndServerPortSpecified.class)
    public HiddenServiceDefinition applicationHiddenServiceDefinition(ServerProperties serverProperties) {
        Integer port = serverProperties.getPort();
        if (port == null) {
            throw new IllegalStateException("Cannot publish hidden service for application. " +
                    "Please specify 'server.port' or disable auto publishing with 'org.tbk.tor.auto-publish-enabled=false'");
        }

        String hiddenServiceDir = String.format("%s/%s", properties.getWorkingDirectory(), "spring_boot_app");

        return HiddenServiceDefinition.builder()
                .directory(new File(hiddenServiceDir))
                .virtualPort(80)
                .port(port)
                .host(Optional.ofNullable(serverProperties.getAddress())
                        .map(InetAddress::getHostAddress)
                        .orElseGet(() -> InetAddress.getLoopbackAddress().getHostName()))
                .build();
    }

}
