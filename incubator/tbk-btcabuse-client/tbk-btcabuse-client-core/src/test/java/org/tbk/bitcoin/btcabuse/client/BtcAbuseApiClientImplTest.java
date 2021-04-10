package org.tbk.bitcoin.btcabuse.client;

//import com.github.tomakehurst.wiremock.junit.WireMockRule;
//import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import org.junit.jupiter.api.Disabled;

@Disabled("Currently needs wiremock which is not compatible with junit5 without wiremock-extension")
public class BtcAbuseApiClientImplTest {

    /*
    @org.junit.RuleRule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private BtcAbuseApiClientImpl sut;

    @BeforeEach
    public void setup() {
        String baseUrl = wireMockRule.baseUrl();
        String apiToken = RandomStringUtils.randomAlphanumeric(10, 20);

        this.sut = new BtcAbuseApiClientImpl(HttpClients.createDefault(), baseUrl, apiToken);

        BitcoinAbuseWireMocked bitcoinAbuseWireMocked = BitcoinAbuseWireMocked.builder()
                .apiToken(apiToken)
                .wireMockRule(wireMockRule)
                .build();

        bitcoinAbuseWireMocked.setupStub();
    }

    @Test
    public void itShouldCheckReportedAddressSuccessfully() {
        CheckResponseDto check = this.sut.check("12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw");

        assertThat(check, is(notNullValue()));
        assertThat(check.getAddress(), is("12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw"));
        assertThat(check.getCount(), is(2L));
    }

    @Test
    public void itShouldCheckUnreportedAddressSuccessfully() {
        CheckResponseDto check = this.sut.check("1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY");

        assertThat(check, is(notNullValue()));
        assertThat(check.getAddress(), is("1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY"));
        assertThat(check.getCount(), is(0L));
    }

    @Builder
    private static class BitcoinAbuseWireMocked {
        private final WireMockRule wireMockRule;
        private final String apiToken;

        public void setupStub() {
            // "check" endpoint with reported address
            wireMockRule.stubFor(get(urlPathEqualTo("/api/reports/check"))
                    .withQueryParams(ImmutableMap.<String, StringValuePattern>builder()
                            .put("address", equalTo("12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw"))
                            .put("api_token", equalTo(apiToken))
                            .build())
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(200)
                            .withBody("{\n" +
                                    "  \"address\": \"12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw\",\n" +
                                    "  \"count\": 2\n" +
                                    "}")));

            // "check" endpoint with unreported address
            wireMockRule.stubFor(get(urlPathEqualTo("/api/reports/check"))
                    .withQueryParams(ImmutableMap.<String, StringValuePattern>builder()
                            .put("address", equalTo("1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY"))
                            .put("api_token", equalTo(apiToken))
                            .build())
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(200)
                            .withBody("{\n" +
                                    "  \"address\": \"1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY\",\n" +
                                    "  \"count\": 0\n" +
                                    "}")));
        }
    }*/
}