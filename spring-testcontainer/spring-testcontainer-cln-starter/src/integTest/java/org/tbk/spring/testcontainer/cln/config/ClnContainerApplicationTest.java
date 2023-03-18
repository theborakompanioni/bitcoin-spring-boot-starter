package org.tbk.spring.testcontainer.cln.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.spring.testcontainer.cln.ClnContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ClnContainerApplicationTest {

    @SpringBootApplication
    public static class ClnContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(ClnContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired(required = false)
    private ClnContainer<?> clnContainer;

    @Test
    void contextLoads() {
        assertThat(clnContainer, is(notNullValue()));
        assertThat(clnContainer.isRunning(), is(true));
    }

    @Test
    void itShouldAcceptJsonrpcRequests() {
        // TODO: actually write the test..
        assertThat(clnContainer, is(notNullValue()));
    }
}
