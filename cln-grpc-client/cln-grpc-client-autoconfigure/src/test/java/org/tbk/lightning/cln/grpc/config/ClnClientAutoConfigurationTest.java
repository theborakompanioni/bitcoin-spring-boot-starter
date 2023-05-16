package org.tbk.lightning.cln.grpc.config;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClnClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(ClnClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.cln.grpc.host=localhost",
                        "org.tbk.lightning.cln.grpc.port=10001",
                        "org.tbk.lightning.cln.grpc.ca-cert-file-path=src/test/resources/cln/test-ca.pem",
                        "org.tbk.lightning.cln.grpc.client-cert-file-path=src/test/resources/cln/test-client.pem",
                        "org.tbk.lightning.cln.grpc.client-key-file-path=src/test/resources/cln/test-client-key.pem"
                )
                .run(context -> {
                    Map<String, Class<?>> beanNamesAndClasses = ImmutableMap.<String, Class<?>>builder()
                            .put("clnRpcSslContext", SslContext.class)
                            .put("clnRpcConfig", ClnRpcConfig.class)
                            .put("clnChannelBuilder", ManagedChannelBuilder.class)
                            .put("clnChannel", ManagedChannel.class)
                            .put("clnChannelShutdownHook", DisposableBean.class)
                            .put("clnNodeStub", NodeGrpc.NodeStub.class)
                            .put("clnNodeBlockingStub", NodeGrpc.NodeBlockingStub.class)
                            .put("clnNodeFutureStub", NodeGrpc.NodeFutureStub.class)
                            .build();

                    beanNamesAndClasses.forEach((name, clazz) -> {
                        assertThat(context.containsBean(name), is(true));
                        assertThat(context.getBean(clazz), is(notNullValue()));
                    });
                });
    }

    @Test
    void beansAreCreatedBase64Values() {
        this.contextRunner.withUserConfiguration(ClnClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.cln.grpc.host=localhost",
                        "org.tbk.lightning.cln.grpc.port=10001",
                        // same `src/test/resources/cln/test-ca.pem` but base64-encoded
                        "org.tbk.lightning.cln.grpc.ca-cert-base64=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUJjVENDQVJlZ0F3SUJBZ0lJZDRBRENObmpwTWN3Q2dZSUtvWkl6ajBFQXdJd0ZqRVVNQklHQTFVRUF3d0wKWTJ4dUlGSnZiM1FnUTBFd0lCY05OelV3TVRBeE1EQXdNREF3V2hnUE5EQTVOakF4TURFd01EQXdNREJhTUJZeApGREFTQmdOVkJBTU1DMk5zYmlCU2IyOTBJRU5CTUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFCjlsK2VTLzBJeGNGQkNuNk9teUVFWXhQRmZieW5NR2xuMjJwMTMxenNIZlA5ZVoyenR4bm9kZUZuSzFoKzlFY1UKcEluR2hmV3VBN29vSTFuekU5cFhMcU5OTUVzd0dRWURWUjBSQkJJd0VJSURZMnh1Z2dsc2IyTmhiR2h2YzNRdwpIUVlEVlIwT0JCWUVGTWVrNDlrSUE0QjNocDNsaW5WT1MwZC9wRXpDTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3CkNnWUlLb1pJemowRUF3SURTQUF3UlFJZ1NZcDRYTEEwYkJ2TXZqbmNZYWRiL0UweXhnUExaZXpGTkV4WjNVWTEKbjJFQ0lRRDJReW1adklIRVludVNSUW5LT0FIRC85YnhxWGdNNWV6bDllVTZoZElQalE9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                        // same `src/test/resources/cln/test-client.pem` but base64-encoded
                        "org.tbk.lightning.cln.grpc.client-cert-base64=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUJRekNCNjZBREFnRUNBZ2haLzZwV3UrWmFWekFLQmdncWhrak9QUVFEQWpBV01SUXdFZ1lEVlFRRERBdGoKYkc0Z1VtOXZkQ0JEUVRBZ0Z3MDNOVEF4TURFd01EQXdNREJhR0E4ME1EazJNREV3TVRBd01EQXdNRm93R2pFWQpNQllHQTFVRUF3d1BZMnh1SUdkeWNHTWdRMnhwWlc1ME1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEClFnQUV4aHRITWhPemE5UnAwdzk1UCtKdzRwL1o4eHZVeVJDTXpEREFrUUdsUkhQZ3lWVkluTzhxYngxdnpZblIKaHlWSmNOV1JXTjRFR1dBcDVpSWtRRFB6STZNZE1Cc3dHUVlEVlIwUkJCSXdFSUlEWTJ4dWdnbHNiMk5oYkdodgpjM1F3Q2dZSUtvWkl6ajBFQXdJRFJ3QXdSQUlnRVFDbVhkQ2p1SG1JaHJGaElsWlo5aithbE5GQ3ZrZ2FPeTBkCkJwNHFZUjRDSUh1Z3Ivc2lnUSt2N2s4czNueEZDYXdNWXRyVGFQb0U1TFpJb1lvWDhqZXYKLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ==",
                        // same `src/test/resources/cln/test-client-key.pem` but base64-encoded
                        "org.tbk.lightning.cln.grpc.client-key-base64=LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR0hBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJHMHdhd0lCQVFRZzFJOTRkZWE2WGxZMFpLZDEKYjFYNWtLdFlIYk1jZGtiMndNa0VpUzhpWTgyaFJBTkNBQVRHRzBjeUU3TnIxR25URDNrLzRuRGluOW56RzlUSgpFSXpNTU1DUkFhVkVjK0RKVlVpYzd5cHZIVy9OaWRHSEpVbHcxWkZZM2dRWllDbm1JaVJBTS9NagotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t"
                )
                .run(context -> {
                    Map<String, Class<?>> beanNamesAndClasses = ImmutableMap.<String, Class<?>>builder()
                            .put("clnNodeBlockingStub", NodeGrpc.NodeBlockingStub.class)
                            .build();

                    beanNamesAndClasses.forEach((name, clazz) -> {
                        assertThat(context.containsBean(name), is(true));
                        assertThat(context.getBean(clazz), is(notNullValue()));
                    });
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(ClnClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.cln.grpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("clnRpcConfig"), is(false));
                    assertThat(context.containsBean("clnNodeStub"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(NodeGrpc.NodeStub.class));
                });
    }

    @Test
    void errorIfCertFileIsMissing() {
        this.contextRunner.withUserConfiguration(ClnClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.cln.grpc.host=localhost",
                        "org.tbk.lightning.cln.grpc.port=10001",
                        "org.tbk.lightning.cln.grpc.ca-cert-file-path=src/test/resources/cln/test-ca-missing.pem",
                        "org.tbk.lightning.cln.grpc.client-cert-file-path=/dev/null",
                        "org.tbk.lightning.cln.grpc.client-key-file-path=/dev/null"
                ).run(context -> {
                    Throwable startupFailure = context.getStartupFailure();
                    assertThat(startupFailure, is(notNullValue()));

                    Throwable rootCause = Throwables.getRootCause(startupFailure);
                    assertThat(rootCause.getMessage(), is("'caCertFile' must exist"));
                });
    }
}
