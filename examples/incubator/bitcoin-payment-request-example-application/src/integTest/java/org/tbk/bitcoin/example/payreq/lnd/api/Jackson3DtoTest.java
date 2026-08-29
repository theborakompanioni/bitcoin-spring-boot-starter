package org.tbk.bitcoin.example.payreq.lnd.api;

import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.example.payreq.donation.Donation;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

// sanity test
class Jackson3DtoTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializeCreateInvoiceRequestDto() {
        LndApi.CreateInvoiceRequestDto createInvoiceRequestDto = objectMapper.readValue("""
                {
                    "msats": 2100,
                    "memo": "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
                }
                """, LndApi.CreateInvoiceRequestDto.class);

        assertThat(createInvoiceRequestDto.getMsats()).isEqualTo(2100L);
        assertThat(createInvoiceRequestDto.getMemo()).isEqualTo("The Times 03/Jan/2009 Chancellor on brink of second bailout for banks");
    }

    @Test
    void deserializeDonation() {
        Donation value = objectMapper.readValue("""
                {
                  "comment" : "hello world.",
                  "createdAt" : "2026-08-29T17:59:47.260Z",
                  "description" : "Donation of 0.00001281 BTC (USD 1) on 2026-08-29T17:59:47.176935149Z",
                  "displayPrice" : "BTC 0.00 001 281",
                  "id" : "7e9b38a6-a7ae-48cc-9cd6-fe71bf4495b1",
                  "new" : false,
                  "order" : {
                    "id" : {
                      "id" : "60c705f7-dd0d-4c95-b705-2da12d6be3f9"
                    }
                  },
                  "paymentRequest" : {
                    "id" : {
                      "id" : "49ba1efd-dfb3-46fb-af45-d05a0550ba4f"
                    }
                  },
                  "paymentUrl" : "bitcoin:bcrt1qj7ap8rralhe5tesnnvhl9vmrn7kvwrmp3prhth?amount=0.00001281"
                }
                """, Donation.class);

        assertThat(value.getComment()).isEqualTo("hello world.");
    }
}