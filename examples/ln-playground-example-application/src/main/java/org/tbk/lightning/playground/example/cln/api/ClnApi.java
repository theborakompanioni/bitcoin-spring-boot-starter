package org.tbk.lightning.playground.example.cln.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.MessageOrBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.lightning.cln.grpc.client.GetinfoRequest;
import org.tbk.lightning.cln.grpc.client.GetinfoResponse;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

import static org.tbk.lightning.playground.example.util.MoreJsonFormat.protoToJson;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/cln", produces = "application/json")
@RequiredArgsConstructor
@Tags({
        @Tag(name = "cln")
})
public class ClnApi {

    @NonNull
    private final NodeGrpc.NodeBlockingStub clnApi;

    @NonNull
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/info")
    public ResponseEntity<JsonNode> getInfo() {
        GetinfoResponse info = clnApi.getinfo(GetinfoRequest.newBuilder().build());
        return ResponseEntity.ok(toJson(info));
    }

    private JsonNode toJson(MessageOrBuilder messageOrBuilder) {
        try {
            return objectMapper.readTree(protoToJson(messageOrBuilder));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
