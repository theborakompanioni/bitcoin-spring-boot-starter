package org.tbk.lightning.regtest.example.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;

/**
 * A controller that provides an API to interact test user node "Alice".
 */
@Slf4j
@Validated
@RestController
@RequestMapping(value = "/api/v1/regtest/test-user-node/alice", produces = MediaType.APPLICATION_JSON_VALUE)
@Tags({
        @Tag(name = "node-alice")
})
public class LocalTestUserAliceClnNodeApi extends AbstractClnNodeApi {

    public LocalTestUserAliceClnNodeApi(@Qualifier("nodeAliceClnNodeBlockingStub") NodeGrpc.NodeBlockingStub node) {
        super(node);
    }
}
