package org.tbk.lightning.playground.example.api.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = CreateInvoiceRequestDto.CreateInvoiceRequestDtoBuilder.class)
public class CreateInvoiceRequestDto {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class CreateInvoiceRequestDtoBuilder {
    }

    @Min(0)
    Long msats;

    String memo;
}
