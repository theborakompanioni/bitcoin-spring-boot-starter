package org.tbk.lightning.lnd.grpc.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoint.health.show-details=always",
        "org.tbk.lightning.lnd.grpc.rpchost=localhost",
        "org.tbk.lightning.lnd.grpc.rpcport=13337",
        "org.tbk.lightning.lnd.grpc.macaroonFilePath=/dev/null",
        "org.tbk.lightning.lnd.grpc.certFilePath=src/test/resources/lnd/tls-test.cert"
})
public class LndJsonRpcInfoContributorIntegrationTest {

    @SpringBootApplication
    public static class BitcoinJsonRpcTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinJsonRpcTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void itShouldAddInformationToInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(jsonPath("lndJsonRpc").exists())
                .andExpect(jsonPath("lndJsonRpc.performValidation").exists())
                .andExpect(status().isOk());
    }
}