package org.tbk.lightning.lnd.grpc.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoint.health.show-details=always",
        "management.health.lndApi.enabled=true",
        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
        "org.tbk.lightning.lnd.grpc.rpcport=13337",
        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
})
public class EnabledLndHealthIndicatorIntegrationTest {

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
    public void itShouldCheckHealthEndpoint() throws Exception {
        // here we just check if the response is well-formed
        // as no lnd is running, we will end up with 503 DOWN
        mockMvc.perform(get("/actuator/health/lndApi"))
                .andExpect(jsonPath("status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("details.performValidation").exists())
                .andExpect(jsonPath("details.status").exists())
                .andExpect(jsonPath("details.status.code").value("UNAVAILABLE"))
                .andExpect(jsonPath("details.status.cause").exists())
                .andExpect(jsonPath("details.error")
                        .value(startsWith("org.lightningj.lnd.wrapper.CommunicationException: UNAVAILABLE")))
                .andExpect(status().is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE));
    }

    @Test
    public void itShouldAddInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(jsonPath("status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("components.lndApi").exists())
                .andExpect(jsonPath("components.lndApi.status").value(Status.DOWN.getCode()))
                .andExpect(jsonPath("components.lndApi.details").exists())
                .andExpect(jsonPath("components.lndApi.details.performValidation").exists())
                .andExpect(jsonPath("components.lndApi.details.status").exists())
                .andExpect(jsonPath("components.lndApi.details.status.code").value("UNAVAILABLE"))
                .andExpect(jsonPath("components.lndApi.details.status.cause").exists())
                .andExpect(jsonPath("components.lndApi.details.error")
                        .value(startsWith("org.lightningj.lnd.wrapper.CommunicationException: UNAVAILABLE")))
                .andExpect(status().is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE));
    }
}