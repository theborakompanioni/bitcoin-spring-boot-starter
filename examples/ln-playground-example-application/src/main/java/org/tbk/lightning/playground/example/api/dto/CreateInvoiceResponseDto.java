package org.tbk.lightning.playground.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CreateInvoiceResponseDto {
    @NonNull
    @Schema(example = "lnbcrt10p1pj...cpcpugtjt8", requiredMode = Schema.RequiredMode.REQUIRED)
    String bolt11;

    Object raw;
}
