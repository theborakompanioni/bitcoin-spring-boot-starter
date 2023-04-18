package org.tbk.lightning.lnd.grpc.config;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.autopilot.AsynchronousAutopilotAPI;
import org.lightningj.lnd.wrapper.chainnotifier.AsynchronousChainNotifierAPI;
import org.lightningj.lnd.wrapper.invoices.AsynchronousInvoicesAPI;
import org.lightningj.lnd.wrapper.router.AsynchronousRouterAPI;
import org.lightningj.lnd.wrapper.signer.AsynchronousSignerAPI;
import org.lightningj.lnd.wrapper.verrpc.AsynchronousVersionerAPI;
import org.lightningj.lnd.wrapper.walletkit.AsynchronousWalletKitAPI;
import org.lightningj.lnd.wrapper.walletunlocker.AsynchronousWalletUnlockerAPI;
import org.lightningj.lnd.wrapper.watchtower.AsynchronousWatchtowerAPI;
import org.lightningj.lnd.wrapper.wtclient.AsynchronousWatchtowerClientAPI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LndClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.host=localhost",
                        "org.tbk.lightning.lnd.grpc.port=10001",
                        "org.tbk.lightning.lnd.grpc.macaroon-file-path=/dev/null",
                        "org.tbk.lightning.lnd.grpc.cert-file-path=src/test/resources/lnd/tls-test.cert"
                )
                .run(context -> {
                    Map<String, Class<?>> beanNamesAndClasses = ImmutableMap.<String, Class<?>>builder()
                            .put("lndAPI", AsynchronousLndAPI.class)
                            .put("lndWalletUnlockerAPI", AsynchronousWalletUnlockerAPI.class)
                            .put("lndAutopilotAPI", AsynchronousAutopilotAPI.class)
                            .put("lndChainNotifierAPI", AsynchronousChainNotifierAPI.class)
                            .put("lndInvoiceAPI", AsynchronousInvoicesAPI.class)
                            .put("lndRouterAPI", AsynchronousRouterAPI.class)
                            .put("lndSignerAPI", AsynchronousSignerAPI.class)
                            .put("lndWalletKitAPI", AsynchronousWalletKitAPI.class)
                            .put("lndWatchtowerAPI", AsynchronousWatchtowerAPI.class)
                            .put("lndWatchtowerClientAPI", AsynchronousWatchtowerClientAPI.class)
                            .put("lndVersionerAPI", AsynchronousVersionerAPI.class)
                            .build();

                    beanNamesAndClasses.forEach((name, clazz) -> {
                        assertThat(context.containsBean(name), is(true));
                        assertThat(context.getBean(clazz), is(notNullValue()));
                    });
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("lndAPI"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(AsynchronousLndAPI.class));
                });
    }

    @Test
    void errorIfCertFileIsMissing() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.host=localhost",
                        "org.tbk.lightning.lnd.grpc.port=10001",
                        "org.tbk.lightning.lnd.grpc.macaroon-file-path=/dev/null",
                        "org.tbk.lightning.lnd.grpc.cert-file-path=src/test/resources/lnd/tls-test-missing.cert"
                ).run(context -> {
                    Throwable startupFailure = context.getStartupFailure();
                    assertThat(startupFailure, is(notNullValue()));

                    Throwable rootCause = Throwables.getRootCause(startupFailure);
                    assertThat(rootCause.getMessage(), is("'certFile' must exist"));
                });
    }
}
