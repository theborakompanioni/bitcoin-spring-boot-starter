package org.tbk.bitcoin.txstats.example.score.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Ignore("Needs access to a mainnet node - refactor to use regtest!")
public class TxScoreCtrlTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void itShouldScoreTransactionSuccessfully() {
        // second tx made by satoshi
        String txId = "0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098";

        UriComponents uriComponents = UriComponentsBuilder.fromPath("/api/v1/tx/score/{txId}")
                .buildAndExpand(ImmutableMap.<String, String>builder()
                        .put("txId", txId)
                        .build());

        RequestEntity<Void> requestEntity = RequestEntity.get(uriComponents.toUri())
                .build();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        Map<String, Object> body = responseEntity.getBody();

        assertThat(body.get("tx_id"), is(txId));

        List<Map<String, Object>> labels = (List<Map<String, Object>>) body.get("labels");
        assertThat(labels, hasSize(greaterThanOrEqualTo(1)));

        Map<String, Object> firstLabel = labels.get(0);
        assertThat(firstLabel.get("name"), is("unknown_miner"));
    }

    @Test
    public void itShouldReturn404WhenTransactionCannotBeFound() {
        // first tx made by satoshi - cannot be fetched with jsonrpc client
        String txId = "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b";

        UriComponents uriComponents = UriComponentsBuilder.fromPath("/api/v1/tx/score/{txId}")
                .buildAndExpand(ImmutableMap.<String, String>builder()
                        .put("txId", txId)
                        .build());

        RequestEntity<Void> requestEntity = RequestEntity.get(uriComponents.toUri())
                .build();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void itShouldThrowErrorIfInputIsNotAValidTxId() {
        String invalidTxId = "1234567890";

        UriComponents uriComponents = UriComponentsBuilder.fromPath("/api/v1/tx/score/{txId}")
                .buildAndExpand(ImmutableMap.<String, String>builder()
                        .put("txId", invalidTxId)
                        .build());

        RequestEntity<Void> requestEntity = RequestEntity.get(uriComponents.toUri())
                .build();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
