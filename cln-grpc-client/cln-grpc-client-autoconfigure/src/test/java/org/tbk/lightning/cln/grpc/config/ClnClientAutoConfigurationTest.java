package org.tbk.lightning.cln.grpc.config;

import cln.NodeGrpc;
import com.google.common.collect.ImmutableMap;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;

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
                        "org.tbk.lightning.cln.grpc.port=10001"
                )
                .run(context -> {
                    Map<String, Class<?>> beanNamesAndClasses = ImmutableMap.<String, Class<?>>builder()
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
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(ClnClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.lightning.cln.grpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("clnNodeStub"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(NodeGrpc.NodeStub.class));
                });
    }
}
