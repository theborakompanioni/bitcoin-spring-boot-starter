package org.tbk.lightning.lnurl.example.api;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.tbk.lightning.lnurl.example.lnurl.LnAuthService;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = LnUrlAuthLoginApi.LNURL_AUTH_PATH, produces = "application/json")
@RequiredArgsConstructor
public class LnUrlAuthLoginApi {
    static final String LNURL_AUTH_PATH = "/api/v1/lnauth/login";

    public static String lnurlAuthPath() {
        return LNURL_AUTH_PATH;
    }

    @NonNull
    private final LnAuthService lnAuthService;

    @NonNull
    private final ApplicationEventPublisher eventPublisher;

    // <LNURL_hostname_and_path>?<LNURL_existing_query_parameters>&sig=<hex(sign(utf8ToBytes(k1), linkingPrivKey))>&key=<hex(linkingKey)>
    @GetMapping
    public ResponseEntity<Object> loginCallback(@RequestParam("k1") String k1ParamHex,
                                                @RequestParam("sig") String sig,
                                                @RequestParam("key") String key) {
        URI currentRequestUri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        log.info("= GOT AUTH REQUEST =========");
        log.info("uri: {}", currentRequestUri);
        log.info("============================");
        log.info("k1: {}", k1ParamHex);
        log.info("sig: {}", sig);
        log.info("key: {}", key);
        log.info("============================");

        boolean loginVerified = lnAuthService.verifyLogin(currentRequestUri);
        if (!loginVerified) {
            log.error("Received invalid lnurl-auth request: {}", currentRequestUri);

            Map<String, String> errorBody = ImmutableMap.<String, String>builder()
                    .put("status", "ERROR")
                    .put("reason", "Request could not be authenticated.")
                    .build();

            return ResponseEntity.badRequest().body(errorBody);
        }
        log.info("AUTH REQUEST IS VALID");
        log.info("============================");

        Map<String, String> successBody = ImmutableMap.<String, String>builder()
                .put("status", "OK")
                .build();

        return ResponseEntity.ok().body(successBody);
    }


}
