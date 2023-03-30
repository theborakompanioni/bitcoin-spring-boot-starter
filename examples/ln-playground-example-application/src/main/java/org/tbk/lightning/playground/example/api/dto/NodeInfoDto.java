package org.tbk.lightning.playground.example.api.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = NodeInfoDto.NodeInfoDtoBuilder.class)
public class NodeInfoDto {
    @JsonPOJOBuilder(withPrefix = "")
    public static final class NodeInfoDtoBuilder {
    }

    @NonNull
    @Schema(example = "034f59...0f0d1d", requiredMode = Schema.RequiredMode.REQUIRED)
    String id;

    @NonNull
    String alias;

    @NonNull
    String version;

    Object raw;
}
