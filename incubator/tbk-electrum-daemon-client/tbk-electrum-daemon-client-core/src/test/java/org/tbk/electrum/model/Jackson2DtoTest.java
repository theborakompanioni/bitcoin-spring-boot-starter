package org.tbk.electrum.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.tbk.electrum.rpc.command.AddressBalanceResponse;

import static org.assertj.core.api.Assertions.assertThat;

// sanity test
class Jackson2DtoTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializeCreateInvoiceRequestDto() throws JsonProcessingException {
        AddressBalanceResponse addressBalanceResponse = objectMapper.readValue("""
                {
                    "confirmed": "0.00002100",
                    "unconfirmed": "2100.00000021"
                }
                """, AddressBalanceResponse.class);

        assertThat(addressBalanceResponse.getConfirmed()).isEqualTo("0.00002100");
        assertThat(addressBalanceResponse.getUnconfirmed()).isEqualTo("2100.00000021");
    }
}