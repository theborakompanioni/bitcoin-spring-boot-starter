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
 * A controller that provides an API to interact test user node "Charlie".
 */
@Slf4j
@Validated
@RestController
@RequestMapping(value = "/api/v1/regtest/test-user-node/charlie", produces = MediaType.APPLICATION_JSON_VALUE)
@Tags({
        @Tag(name = "node-charlie")
})
public class LocalTestUserCharlieClnNodeApi extends AbstractClnNodeApi {

    public LocalTestUserCharlieClnNodeApi(@Qualifier("nodeCharlieClnNodeBlockingStub")
                                                 NodeGrpc.NodeBlockingStub node) {
        super(node);
    }
}
