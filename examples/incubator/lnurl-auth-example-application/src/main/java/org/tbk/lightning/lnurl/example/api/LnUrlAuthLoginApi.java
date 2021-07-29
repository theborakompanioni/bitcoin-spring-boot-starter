package org.tbk.lightning.lnurl.example.api;

import com.google.common.collect.ImmutableMap;
import fr.acinq.secp256k1.Hex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.tbk.lightning.lnurl.example.domain.WalletUser;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lnurl.K1;
import org.tbk.lnurl.simple.SimpleK1;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = LnUrlAuthLoginApi.LNURL_AUTH_PATH, produces = "application/json")
@RequiredArgsConstructor
public class LnUrlAuthLoginApi {
    static final String LNURL_AUTH_PATH = "/api/v1/lnauth/login";

    public static String lnurlAuthLoginPath() {
        return LNURL_AUTH_PATH;
    }

    private final WalletUserService walletUserService;

    // <LNURL_hostname_and_path>?<LNURL_existing_query_parameters>&sig=<hex(sign(utf8ToBytes(k1), linkingPrivKey))>&key=<hex(linkingKey)>
    @GetMapping
    public ResponseEntity<Object> loginCallback(@RequestParam("k1") String k1ParamHex,
                                                @RequestParam("sig") String sigParamHex,
                                                @RequestParam("key") String keyParamHex) {
        URI currentRequestUri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        log.info("= GOT AUTH REQUEST =========");
        log.info("uri: {}", currentRequestUri);
        log.info("============================");
        log.info("k1: {}", k1ParamHex);
        log.info("sig: {}", sigParamHex);
        log.info("key: {}", keyParamHex);
        log.info("============================");

        K1 k1 = SimpleK1.fromHex(k1ParamHex);
        byte[] sig = Hex.decode(sigParamHex);
        byte[] key = Hex.decode(keyParamHex);

        try {
            WalletUser user = walletUserService.login(key, sig, k1);

            log.info("AUTH REQUEST IS VALID");
            log.info("============================");
            log.info("user: {} ({})", user.getId(), user.getName());
            log.info("============================");

            Map<String, String> successBody = ImmutableMap.<String, String>builder()
                    .put("status", "OK")
                    .build();

            return ResponseEntity.ok().body(successBody);
        } catch (Exception e) {
            log.error("Received invalid lnurl-auth request '{}': {}", currentRequestUri, e.getMessage());

            Map<String, String> errorBody = ImmutableMap.<String, String>builder()
                    .put("status", "ERROR")
                    .put("reason", "Request could not be authenticated.")
                    .build();

            return ResponseEntity.badRequest().body(errorBody);
        }
    }
}
