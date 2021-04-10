package org.tbk.bitcoin.btcabuse.client;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;
import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class BtcAbuseApiClientImplTest {

    private static final String DUMMY_API_TOKEN = RandomStringUtils.randomAlphanumeric(10, 20);

    private final MockServerClient client;

    private BtcAbuseApiClientImpl sut;

    BtcAbuseApiClientImplTest(MockServerClient client) {
        this.client = requireNonNull(client);

        BitcoinAbuseMockedApiSetup bitcoinAbuseWireMocked = BitcoinAbuseMockedApiSetup.builder()
                .client(client)
                .build();

        bitcoinAbuseWireMocked.setupStub();
    }

    @BeforeEach
    void setup() {
        String baseUrl = "http://localhost:" + client.getPort();

        this.sut = new BtcAbuseApiClientImpl(HttpClients.createDefault(), baseUrl, DUMMY_API_TOKEN);

    }

    @Test
    void itShouldCheckReportedAddressSuccessfully() {
        CheckResponseDto check = this.sut.check("12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw");

        assertThat(check, is(notNullValue()));
        assertThat(check.getAddress(), is("12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw"));
        assertThat(check.getCount(), is(2L));
    }

    @Test
    void itShouldCheckUnreportedAddressSuccessfully() {
        CheckResponseDto check = this.sut.check("1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY");

        assertThat(check, is(notNullValue()));
        assertThat(check.getAddress(), is("1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY"));
        assertThat(check.getCount(), is(0L));
    }

    @Builder
    private static class BitcoinAbuseMockedApiSetup {
        private final MockServerClient client;

        public void setupStub() {
            // "check" endpoint with reported address
            client.when(request()
                    .withPath("/api/reports/check")
                    .withQueryStringParameters(ImmutableList.<Parameter>builder()
                            .add(Parameter.param("address", "12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw"))
                            .build())
            ).respond(response()
                    .withHeader(Header.header("Content-Type", "application/json"))
                    .withStatusCode(200)
                    .withBody("{\n" +
                            "  \"address\": \"12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw\",\n" +
                            "  \"count\": 2\n" +
                            "}"));

            // "check" endpoint with unreported address
            client.when(request()
                    .withPath("/api/reports/check")
                    .withQueryStringParameters(ImmutableList.<Parameter>builder()
                            .add(Parameter.param("address", "1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY"))
                            .build())
            ).respond(response()
                    .withHeader(Header.header("Content-Type", "application/json"))
                    .withStatusCode(200)
                    .withBody("{\n" +
                            "  \"address\": \"1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY\",\n" +
                            "  \"count\": 0\n" +
                            "}"));
        }
    }
}