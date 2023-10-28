package org.tbk.lightning.playground.example.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreateInvoiceRequestDto {

    @Min(0)
    Long msats;

    String memo;
}
