package org.tbk.lightning.playground.example.api.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = CreateInvoiceResponseDto.CreateInvoiceResponseDtoBuilder.class)
public class CreateInvoiceResponseDto {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class CreateInvoiceResponseDtoBuilder {
    }

    @NonNull
    @Schema(example = "lnbcrt10p1pj...cpcpugtjt8", requiredMode = Schema.RequiredMode.REQUIRED)
    String bolt11;

    Object raw;
}
