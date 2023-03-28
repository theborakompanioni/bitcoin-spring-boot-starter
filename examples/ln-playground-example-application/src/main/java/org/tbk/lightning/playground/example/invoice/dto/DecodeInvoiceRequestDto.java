package org.tbk.lightning.playground.example.invoice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = DecodeInvoiceRequestDto.DecodeInvoiceRequestDtoBuilder.class)
public class DecodeInvoiceRequestDto {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class DecodeInvoiceRequestDtoBuilder {
    }

    // invoice taken from https://www.bolt11.org/
    @NonNull
    @Schema(example = "lnbc15u1p3xnhl2pp5jptserfk3zk4qy42tlucycrfwxhydvlemu9pqr93tuzlv9cc7g3sdqsvfhkcap3xyhx7un8cqzpgxqzjcsp5f8c52y2stc300gl6s4xswtjpc37hrnnr3c9wvtgjfuvqmpm35evq9qyyssqy4lgd8tj637qcjp05rdpxxykjenthxftej7a2zzmwrmrl70fyj9hvj0rewhzj7jfyuwkwcg9g2jpwtk3wkjtwnkdks84hsnu8xps5vsq4gj5hs")
    String invoice;

}
