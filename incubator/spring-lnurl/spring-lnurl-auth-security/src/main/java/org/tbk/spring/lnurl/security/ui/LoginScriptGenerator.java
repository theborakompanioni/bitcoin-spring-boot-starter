package org.tbk.spring.lnurl.security.ui;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.JavaScriptUtils;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
final class LoginScriptGenerator {

    @Value
    @Builder
    public static class ScriptConfig {
        public static ScriptConfigBuilder builder() {
            return new ScriptConfigBuilder() {
                @Override
                public ScriptConfig build() {
                    ScriptConfig build = super.build();

                    checkArgument(!build.getInitialDelay().isNegative());
                    checkArgument(build.getPollingInterval().compareTo(Duration.ofSeconds(1)) >= 0);
                    checkArgument(build.getMaxAttempts() >= 0);
                    return build;
                }
            };
        }

        String sessionMigrateEndpoint;

        @Builder.Default
        Duration initialDelay = Duration.ofSeconds(3);

        @Builder.Default
        Duration pollingInterval = Duration.ofSeconds(3);

        @Builder.Default
        int maxAttempts = 100;
    }

    private final String script;

    LoginScriptGenerator(ScriptConfig config) {
        requireNonNull(config);

        //this.script = createInnerScript(sessionMigrateEndpoint, initialDelay, pollingInterval, maxAttempts);

        String scriptTemplate = StaticTemplateUtils.readContents("login.js");
        this.script = scriptTemplate
                .replace("%%_LNURL_AUTH_SESSION_MIGRATION_ENDPOINT_%%", JavaScriptUtils.javaScriptEscape(config.getSessionMigrateEndpoint()))
                .replace("%%_LNURL_AUTH_INITIAL_DELAY_%%", "" + config.getInitialDelay().toMillis())
                .replace("%%_LNURL_AUTH_POLLING_INTERVAL_%%", "" + config.getPollingInterval().toMillis())
                .replace("%%_LNURL_AUTH_MAX_ATTEMPTS_%%", "" + config.getMaxAttempts());
    }

    public String createScript() {
        return script;
    }
}