package org.tbk.lightning.playground.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class NodeInfoDto {
    @NonNull
    @Schema(example = "034f59...0f0d1d", requiredMode = Schema.RequiredMode.REQUIRED)
    String id;

    @NonNull
    String alias;

    @NonNull
    String version;

    Object raw;
}
