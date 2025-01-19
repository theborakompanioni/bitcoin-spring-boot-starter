package org.tbk.lightning.playground.example.cln.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class ClnApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldFetchInfo() throws Exception {
        mockMvc.perform(get("/api/v1/cln/info"))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("alias").isString())
                .andExpect(jsonPath("version").isString())
                .andExpect(jsonPath("raw").exists())
                .andExpect(status().isOk());
    }

    @Test
    void itShouldCreateInvoice() throws Exception {
        mockMvc.perform(post("/api/v1/cln/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "msats": 2100,
                                    "memo": "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
                                }
                                """))
                .andExpect(jsonPath("bolt11").value(startsWith("lnbcrt")))
                .andExpect(jsonPath("raw").exists())
                .andExpect(status().isOk());
    }
}
