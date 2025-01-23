package org.tbk.bitcoin.exchange.example.api.rate;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExchangeRateCtrlTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void itShouldFetchExchangeRateFromKrakenSuccessfully() {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("/api/v1/exchange/latest?base={base}&target={target}&provider={provider}")
                .buildAndExpand(ImmutableMap.<String, String>builder()
                        .put("base", "BTC")
                        .put("target", "USD")
                        .put("provider", "KRAKEN")
                        .build());

        RequestEntity<Void> requestEntity = RequestEntity.get(uriComponents.toUri())
                .build();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        Map<String, Object> body = responseEntity.getBody();

        assertThat(body, is(notNullValue()));
        assertThat(body.get("base"), is("BTC"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rates = (List<Map<String, Object>>) body.get("rates");
        assertThat(rates, hasSize(1));

        Map<String, Object> rate = rates.get(0);
        assertThat(rate.get("provider"), is("KRAKEN"));
        assertThat(rate.get("base"), is("BTC"));
        assertThat(rate.get("target"), is("USD"));
        assertThat(rate.get("type"), is("DEFERRED"));
        assertThat(rate.get("derived"), is(Boolean.FALSE));
        assertThat(rate.get("factor"), is(notNullValue()));
        assertThat(rate.get("meta"), is(notNullValue()));
    }
}
