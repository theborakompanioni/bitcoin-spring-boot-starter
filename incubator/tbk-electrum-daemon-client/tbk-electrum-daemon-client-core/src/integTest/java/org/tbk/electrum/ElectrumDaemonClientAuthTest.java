package org.tbk.electrum;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.command.GetInfoResponse;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcConfig;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;

import java.lang.reflect.UndeclaredThrowableException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ElectrumDaemonClientAuthTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class ElectrumDaemonAuthTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ElectrumDaemonAuthTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired(required = false)
    private ElectrumDaemonContainer<?> electrumDaemonContainer;

    @Autowired(required = false)
    private ElectrumxContainer<?> electrumxContainer;

    @Autowired(required = false)
    ElectrumDaemonJsonrpcConfig electrumDaemonJsonrpcConfig;

    @Autowired(required = false)
    ElectrumClientFactory electrumClientFactory;

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(electrumDaemonJsonrpcConfig, is(notNullValue()));
        assertThat(electrumClientFactory, is(notNullValue()));
        assertThat(electrumDaemonContainer, is(notNullValue()));
        assertThat("electrum daemon container is running", electrumDaemonContainer.isRunning(), is(true));

        assertThat(electrumxContainer, is(notNullValue()));
        assertThat("electrumx container is running", electrumxContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(electrumDaemonContainer).block();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void itShouldVerifyAuthenticationError() {
        ElectrumClient unauthenticatedClient = electrumClientFactory.create(
                electrumDaemonJsonrpcConfig.getUri(),
                "electrum",
                "WRONG_PASSWORD"
        );

        UndeclaredThrowableException e = Assertions.assertThrows(UndeclaredThrowableException.class, unauthenticatedClient::getInfo);

        assertThat(e, is(instanceOf(UndeclaredThrowableException.class)));
        assertThat(e.getUndeclaredThrowable().getMessage(), containsString("Forbidden"));
    }

    @Test
    void itShouldVerifyAuthenticationSuccess() {
        ElectrumClient client = electrumClientFactory.create(
                electrumDaemonJsonrpcConfig.getUri(),
                electrumDaemonJsonrpcConfig.getUsername(),
                electrumDaemonJsonrpcConfig.getPassword()
        );

        assertThat(client.getInfo().getNetwork(), is("regtest"));
    }
}

