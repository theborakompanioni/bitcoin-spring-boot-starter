package org.tbk.lightning.playground.example.cln.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.MessageOrBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbk.lightning.cln.grpc.client.*;
import org.tbk.lightning.playground.example.api.dto.CreateInvoiceResponseDto;
import org.tbk.lightning.playground.example.api.dto.NodeInfoDto;
import org.testcontainers.shaded.com.google.common.primitives.Longs;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import static org.tbk.lightning.playground.example.util.MoreJsonFormat.protoToJson;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/cln", produces = "application/json")
@RequiredArgsConstructor
@Tags({
        @Tag(name = "cln")
})
public class ClnApi {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static String randomBytes32() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    @NonNull
    private final NodeGrpc.NodeBlockingStub clnApi;

    @NonNull
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Fetch node info"
    )
    @GetMapping(value = "/info")
    public ResponseEntity<NodeInfoDto> getInfo() {
        GetinfoResponse info = clnApi.getinfo(GetinfoRequest.newBuilder().build());
        return ResponseEntity.ok(NodeInfoDto.builder()
                .id(HexFormat.of().formatHex(info.getId().toByteArray()))
                .alias(info.getAlias())
                .version(info.getVersion())
                .raw(toJson(info))
                .build());
    }

    @Operation(
            summary = "Create invoice"
    )
    @PostMapping(value = "/invoice")
    public ResponseEntity<CreateInvoiceResponseDto> addInvoice(@RequestBody Map<String, Object> body) {

        String memo = Optional.ofNullable(body.get("memo"))
                .map(Object::toString)
                .orElse("");

        long msats = Optional.ofNullable(body.get("msats"))
                .map(Object::toString)
                .map(it -> Longs.tryParse(it, 10))
                .filter(it -> it > 0)
                .orElseThrow(() -> new IllegalArgumentException("'value' must be a positive integer"));

        InvoiceResponse addInvoiceResponse = clnApi.invoice(InvoiceRequest.newBuilder()
                .setAmountMsat(AmountOrAny.newBuilder()
                        .setAmount(Amount.newBuilder()
                                .setMsat(msats)
                                .build())
                        .build())
                .setDescription(memo)
                // label must be unique
                .setLabel(randomBytes32())
                .build());

        return ResponseEntity.ok(CreateInvoiceResponseDto.builder()
                .bolt11(addInvoiceResponse.getBolt11())
                .raw(toJson(addInvoiceResponse))
                .build());
    }

    private JsonNode toJson(MessageOrBuilder messageOrBuilder) {
        try {
            return objectMapper.readTree(protoToJson(messageOrBuilder));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
