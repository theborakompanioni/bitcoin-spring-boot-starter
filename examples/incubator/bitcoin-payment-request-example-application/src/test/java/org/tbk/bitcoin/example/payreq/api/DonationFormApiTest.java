package org.tbk.bitcoin.example.payreq.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tbk.bitcoin.example.payreq.donation.api.DonationApi;
import org.tbk.bitcoin.example.payreq.donation.api.DonationFormApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class DonationFormApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DonationFormApi sut;

    @Autowired
    private DonationApi donationApi;

    @Test
    void itShouldCreateDonationSuccessfully() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/donation/form")
                .accept(MediaType.TEXT_HTML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("network", "regtest")
                .param("amount", "1")
                .param("currency", "USD")
                .param("comment", "hello world."))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/donation.html?donation_id=*"))
                .andReturn();

        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        Matcher matcher = Pattern.compile(".*donation_id=(.*)").matcher(redirectedUrl);
        assertThat(matcher.find(), is(true));
        String donationId = matcher.group(1);

        mockMvc.perform(get("/api/v1/donation/{id}", donationId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(donationId)));
    }

    @Test
    void itShouldReturnErrorOnInvalidRequests() throws Exception {
        mockMvc.perform(post("/api/v1/donation/form")
                .accept(MediaType.TEXT_HTML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());

        // missing "amount"
        mockMvc.perform(post("/api/v1/donation/form")
                .accept(MediaType.TEXT_HTML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("network", "regtest")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        // missing "currency"
        mockMvc.perform(post("/api/v1/donation/form")
                .accept(MediaType.TEXT_HTML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("network", "regtest")
                .param("amount", "1"))
                .andExpect(status().isBadRequest());
    }

}