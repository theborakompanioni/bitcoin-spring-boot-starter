package org.tbk.lightning.playground.example.invoice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = DecodeInvoiceResponseDto.DecodeInvoiceResponseDtoBuilder.class)
public class DecodeInvoiceResponseDto {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class DecodeInvoiceResponseDtoBuilder {
    }

    @NonNull
    @Schema(example = "a9014f...fcb044", requiredMode = Schema.RequiredMode.REQUIRED)
    String paymentHash;

    @NonNull
    String paymentSecret;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String paymentMetadata;

    String features;

    @NonNull
    String pubkey;

    @Schema(example = "2100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long amountMsat;

    @Schema(example = "My payment description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String description;

    @Schema(example = "288ea...b1d488", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String descriptionHash;

    Long timestamp;

    Long expirySeconds;

    Long minFinalCltvExpiry;

    @Schema(example = "bcrt1q...yxqc4j", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String fallbackAddress;

    @NonNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String signature;

    @Singular("routingInfo")
    List<RoutingInfoDto> routingInfos;


    @Value
    @Builder(toBuilder = true)
    @JsonDeserialize(builder = RoutingInfoDto.RoutingInfoDtoBuilder.class)
    public static class RoutingInfoDto {
        @JsonPOJOBuilder(withPrefix = "")
        public static final class RoutingInfoDtoBuilder {
        }

        @Singular("routingHint")
        List<RoutingHintDto> routingHints;
    }

    /**
     * {
     * "pubkey": "03c8abaf1466af2ed3e4f57eb413c47c4fa177d4685933620dfdc717749795dfe0",
     * "short_channel_id": "0bac220000c80000",
     * "fee_base_msat": 1000,
     * "fee_proportional_millionths": 1,
     * "cltv_expiry_delta": 40
     * }
     */
    @Value
    @Builder(toBuilder = true)
    @JsonDeserialize(builder = RoutingHintDto.RoutingHintDtoBuilder.class)
    public static class RoutingHintDto {
        @JsonPOJOBuilder(withPrefix = "")
        public static final class RoutingHintDtoBuilder {
        }

        String pubkey;
        String shortChannelId;
        Long feeBaseMsat;
        Long feeProportionalMillionths;
        Long cltvExpiryDelta;
    }
}
