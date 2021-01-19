package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.io.File;
import java.net.InetAddress;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication
@AutoConfigureBefore(TorAutoConfiguration.class)
public class TorHiddenServiceAutoConfiguration {

    private final TorAutoConfigProperties properties;

    public TorHiddenServiceAutoConfiguration(TorAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Primary
    @Bean("applicationHiddenServiceDefinition")
    @ConditionalOnMissingBean(name = "applicationHiddenServiceDefinition")
    @ConditionalOnBean(ServerProperties.class)
    @Conditional(OnAutoPublishEnabledAndServerPortSpecified.class)
    public HiddenServiceDefinition applicationHiddenServiceDefinition(ServerProperties serverProperties,
                                                                      TorAutoConfigProperties properties) {
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
                        .orElseGet(() -> InetAddress.getLoopbackAddress().getHostAddress()))
                .build();
    }

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

        @ConditionalOnMissingBean(HiddenServiceDefinition.class)
        static class OnMissingBean {
            // this conditional is necessary till netlayer supports more than one hidden service in torrc file
        }
    }

    /**
     * The following code does not work, as class {@link org.berndpruenster.netlayer.tor.Torrc} uses
     * a map as key value store, and all subsequent HiddenServiceDir/HiddenServicePort combinations will
     * be overwritten : /
     * try again later or write a pull request for the project. this is currently just "nice to have" feature..
     */
    @Profile("tbk-tor-try-also-to-publish-management-port-as-distinct-hidden-service---disabled-danger-danger-danger")
    @Configuration
    @ConditionalOnClass({ManagementContextAutoConfiguration.class, ManagementServerProperties.class})
    @AutoConfigureBefore(TorAutoConfiguration.class)
    @AutoConfigureAfter(ManagementContextAutoConfiguration.class)
    public static class ManagementApplicationHiddenServicePublishAutoConfiguration {
        static class OnAutoPublishEnabledAndManagementServerPortSpecified extends AllNestedConditions {

            OnAutoPublishEnabledAndManagementServerPortSpecified() {
                super(ConfigurationPhase.PARSE_CONFIGURATION);
            }

            @ConditionalOnProperty(name = "org.tbk.tor.auto-publish-enabled", havingValue = "true", matchIfMissing = true)
            static class OnAutoPublishEnabled {
            }

            @ConditionalOnProperty(name = "management.server.port")
            static class OnManagementServerPortSpecified {
            }

            @ConditionalOnMissingBean(HiddenServiceDefinition.class)
            static class OnMissingBean {
                // this conditional is necessary till netlayer supports more than one hidden service in torrc file
            }
        }

        @Bean("applicationManagementHiddenServiceDefinition")
        @ConditionalOnMissingBean(name = "applicationManagementHiddenServiceDefinition")
        @ConditionalOnBean(ManagementServerProperties.class)
        @Conditional(OnAutoPublishEnabledAndManagementServerPortSpecified.class)
        public HiddenServiceDefinition applicationManagementHiddenServiceDefinition(ManagementServerProperties managementServerProperties,
                                                                                    TorAutoConfigProperties properties) {
            Integer managementPort = managementServerProperties.getPort();
            if (managementPort == null) {
                throw new IllegalStateException("Cannot publish hidden service for application management. " +
                        "Please specify 'management.server.port' or disable auto publishing with 'org.tbk.tor.auto-publish-enabled=false'");
            }

            String hiddenServiceDir = String.format("%s/%s", properties.getWorkingDirectory(), "spring_boot_app_management");

            return HiddenServiceDefinition.builder()
                    .directory(new File(hiddenServiceDir))
                    .virtualPort(managementPort)
                    .port(managementPort)
                    .host(Optional.ofNullable(managementServerProperties.getAddress())
                            .map(InetAddress::getHostAddress)
                            .orElseGet(() -> InetAddress.getLoopbackAddress().getHostName()))
                    .build();
        }
    }
}
