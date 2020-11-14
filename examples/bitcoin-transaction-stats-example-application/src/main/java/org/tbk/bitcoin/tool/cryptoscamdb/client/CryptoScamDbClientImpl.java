package org.tbk.bitcoin.tool.cryptoscamdb.client;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.requireNonNull;

public class CryptoScamDbClientImpl implements CryptoScamDbClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public CryptoScamDbClientImpl(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = requireNonNull(baseUrl);
        this.restTemplate = requireNonNull(restTemplate);
    }

    @Override
    public AddressesResponseDto addresses() {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUrl + "/v1/addresses")
                .buildAndExpand(ImmutableMap.builder()
                        .build());

        return restTemplate.getForObject(uriComponents.toUri(), AddressesResponseDto.class);
    }

    @Override
    public CheckResponseDto check(@NonNull String address) {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUrl + "/v1/check/{address}")
                .buildAndExpand(ImmutableMap.builder()
                        .put("address", address)
                        .build());

        return restTemplate.getForObject(uriComponents.toUri(), CheckResponseDto.class);
    }

}
