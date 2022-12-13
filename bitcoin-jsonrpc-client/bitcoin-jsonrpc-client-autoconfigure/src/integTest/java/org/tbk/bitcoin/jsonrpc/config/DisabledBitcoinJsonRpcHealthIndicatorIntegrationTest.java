package org.tbk.bitcoin.jsonrpc.config;

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
        "management.health.bitcoinJsonRpc.enabled=false",
        "org.tbk.bitcoin.jsonrpc.network=regtest",
        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
})
class DisabledBitcoinJsonRpcHealthIndicatorIntegrationTest {

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
    void itShouldCheckHiddenServiceHealthEndpointDoesNotExist() throws Exception {
        mockMvc.perform(get("/actuator/health/bitcoinJsonRpc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void itShouldNotAddHiddenServiceInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("status").value("UP"))
                .andExpect(jsonPath("components.bitcoinJsonRpc").doesNotExist())
                .andExpect(status().isOk());
    }
}
