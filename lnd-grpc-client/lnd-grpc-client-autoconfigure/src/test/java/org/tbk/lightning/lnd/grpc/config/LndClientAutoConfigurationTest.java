package org.tbk.lightning.lnd.grpc.config;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.autopilot.AsynchronousAutopilotAPI;
import org.lightningj.lnd.wrapper.chainkit.AsynchronousChainKitAPI;
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

import java.security.cert.CertificateException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
                            .put("lndChainKitAPI", AsynchronousChainKitAPI.class)
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
    void beansAreCreatedBase64Values() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.host=localhost",
                        "org.tbk.lightning.lnd.grpc.port=10001",
                        "org.tbk.lightning.lnd.grpc.macaroon-base64=yv66vg==",
                        // same as `src/test/resources/lnd/tls-test.cert` but base64-encoded
                        "org.tbk.lightning.lnd.grpc.cert-base64=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNCRENDQWF1Z0F3SUJBZ0lRUVZIcmplYmIzcTR1dnFHb3I0bjd2VEFLQmdncWhrak9QUVFEQWpBNE1SOHcKSFFZRFZRUUtFeFpzYm1RZ1lYVjBiMmRsYm1WeVlYUmxaQ0JqWlhKME1SVXdFd1lEVlFRREV3eGlaVEE0TlRrMApOVGc1TVRVd0hoY05NakF4TVRJNE1UZzFNRFU1V2hjTk1qSXdNVEl6TVRnMU1EVTVXakE0TVI4d0hRWURWUVFLCkV4WnNibVFnWVhWMGIyZGxibVZ5WVhSbFpDQmpaWEowTVJVd0V3WURWUVFERXd4aVpUQTROVGswTlRnNU1UVXcKV1RBVEJnY3Foa2pPUFFJQkJnZ3Foa2pPUFFNQkJ3TkNBQVF4eUpJa2tnbHdjQ0xpOTJTMDZta0oxaEVTcVRqQwo5bWliREx2TkpUbnNuRUx2TExmNVRFc1ZQL09yNzdPRjREZjJxcisva29qUUIzTE1pQytyM2ZIV280R1dNSUdUCk1BNEdBMVVkRHdFQi93UUVBd0lDcERBVEJnTlZIU1VFRERBS0JnZ3JCZ0VGQlFjREFUQVBCZ05WSFJNQkFmOEUKQlRBREFRSC9NRnNHQTFVZEVRUlVNRktDREdKbE1EZzFPVFExT0RreE5ZSUpiRzlqWVd4b2IzTjBnZ1IxYm1sNApnZ3AxYm1sNGNHRmphMlYwZ2dkaWRXWmpiMjV1aHdSL0FBQUJoeEFBQUFBQUFBQUFBQUFBQUFBQUFBQUJod1NzCkVRQUZNQW9HQ0NxR1NNNDlCQU1DQTBjQU1FUUNJRWJzdXdCL0hWbkxNd0ZEWnBzZnBvMWhqVEYyWUlSUUo0USsKYlBjclJWUDlBaUJCdmduR0FpRXFSRnN4bXlIb0V1c2dMOStiT0NwWXBtTDlTd0F2YWt1N1p3PT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ=="
                )
                .run(context -> {
                    Map<String, Class<?>> beanNamesAndClasses = ImmutableMap.<String, Class<?>>builder()
                            .put("lndAPI", AsynchronousLndAPI.class)
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

    @Test
    void errorIfCertFileIsInvalid() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.host=localhost",
                        "org.tbk.lightning.lnd.grpc.port=10001",
                        "org.tbk.lightning.lnd.grpc.macaroon-file-path=/dev/null",
                        "org.tbk.lightning.lnd.grpc.cert-file-path=/dev/null"
                ).run(context -> {
                    Throwable startupFailure = context.getStartupFailure();
                    assertThat(startupFailure, is(notNullValue()));

                    Throwable rootCause = Throwables.getRootCause(startupFailure);
                    assertThat(rootCause, instanceOf(CertificateException.class));
                    assertThat(rootCause.getMessage(), is("found no certificates in input stream"));
                });
    }

    @Test
    void errorIfCertBase64Invalid() {
        this.contextRunner.withUserConfiguration(LndClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.lnd.grpc.host=localhost",
                        "org.tbk.lightning.lnd.grpc.port=10001",
                        "org.tbk.lightning.lnd.grpc.macaroon-base64=yv66vg==",
                        "org.tbk.lightning.lnd.grpc.cert-base64=$invalid$"
                ).run(context -> {
                    Throwable startupFailure = context.getStartupFailure();
                    assertThat(startupFailure, is(notNullValue()));

                    Throwables.getCausalChain(startupFailure).stream()
                            .filter(it -> "Error while decoding 'certBase64'".equals(it.getMessage()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Expected exception not found"));

                    Throwable rootCause = Throwables.getRootCause(startupFailure);
                    assertThat(rootCause.getMessage(), is("Illegal base64 character 24"));
                });
    }
}
