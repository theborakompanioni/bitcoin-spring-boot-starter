package org.tbk.lightning.lnd.grpc.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoints.web.exposure.include=info",
        "org.tbk.lightning.lnd.grpc.host=localhost",
        "org.tbk.lightning.lnd.grpc.port=13337",
        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
})
class LndInfoContributorIntegrationTest {

    @SpringBootApplication
    public static class LndInfoContributorTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(LndInfoContributorTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldAddInformationToInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(jsonPath("lndApi").exists())
                .andExpect(jsonPath("lndApi.performValidation").exists())
                .andExpect(status().isOk());
    }
}