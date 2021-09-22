package org.tbk.lightning.lnd.grpc.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoint.health.show-details=always",
        "management.health.lndApi.enabled=false",
        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
        "org.tbk.lightning.lnd.grpc.rpcport=13337",
        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
})
public class DisabledLndHealthIndicatorIntegrationTest {

    @SpringBootApplication
    public static class LndHealthIndicatorTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(LndHealthIndicatorTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void itShouldCheckHealthEndpointDoesNotExist() throws Exception {
        mockMvc.perform(get("/actuator/health/lndAPI"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void itShouldNotAddHiddenServiceInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("status").value("UP"))
                .andExpect(jsonPath("components.lndAPI").doesNotExist())
                .andExpect(status().isOk());
    }
}