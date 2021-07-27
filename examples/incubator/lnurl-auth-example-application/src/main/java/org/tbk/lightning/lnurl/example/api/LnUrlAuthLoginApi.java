package org.tbk.lightning.lnurl.example.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;


@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnauth/login", produces = "application/json")
@RequiredArgsConstructor
public class LnUrlAuthLoginApi {

    // <LNURL_hostname_and_path>?<LNURL_existing_query_parameters>&sig=<hex(sign(utf8ToBytes(k1), linkingPrivKey))>&key=<hex(linkingKey)>
    @GetMapping
    public ResponseEntity<Object> loginCallback(@RequestParam("k1") String k1,
                                                @RequestParam("sig") String sig,
                                                @RequestParam("key") String key) {
        log.info("GOT AUTH REQUEST ============================");
        UriComponents build = ServletUriComponentsBuilder.fromCurrentRequest().build();
        log.info("uri: {}", build.toUriString());
        log.info("============================");
        log.info("k1: {}", k1);
        log.info("sig: {}", sig);
        log.info("key: {}", key);
        log.info("============================");

        return ResponseEntity.badRequest().build();
    }
}
