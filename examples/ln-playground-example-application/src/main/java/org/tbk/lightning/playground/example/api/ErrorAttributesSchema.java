package org.tbk.lightning.playground.example.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Only used as schema reference as {@link DefaultErrorAttributes} does not have
 * properties that can be used in the OpenAPI definition.
 *
 * <p>e.g.
 *
 * <p>{
 * "timestamp": "2019-01-17T16:12:45.977+0000",
 * "status": 500,
 * "error": "Internal Server Error",
 * "message": "Error processing the request!",
 * "path": "/my-endpoint-with-exceptions"
 * }
 */
@Value
@Builder
@Schema(
        name = "ApiError"
)
public class ErrorAttributesSchema {
    @Schema(example = "2019-01-17T16:12:45.977+0000")
    String timestamp;

    @Schema(example = "500")
    int status;

    @Schema(example = "Internal Server Error")
    String error;

    @Schema(example = "Error processing the request!")
    String message;

    @Schema(example = "/my-endpoint-with-exceptions")
    String path;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String trace;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<ObjectError> errors;
}
