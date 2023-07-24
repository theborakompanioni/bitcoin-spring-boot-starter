package org.tbk.bitcoin.regtest.example.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lightning.regtest.example.api.LocalTestUserAliceClnNodeApi;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LocalTestUserAliceClnNodeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocalTestUserAliceClnNodeApi localTestUserAliceClnNodeApi;

    @Test
    void itShouldFetchStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/regtest/test-user-node/alice/balance-info")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channelCount", is(1)))
                .andExpect(jsonPath("$.totalCapacityMsat", is(notNullValue())))
                .andExpect(jsonPath("$.outboundMsat", is(notNullValue())))
                .andExpect(jsonPath("$.inboundMsat", is(notNullValue())))
                .andExpect(jsonPath("$.utxoCount", is(notNullValue())))
                .andExpect(jsonPath("$.onchainMsat", is(notNullValue())));
    }
}