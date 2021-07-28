package org.tbk.lightning.lnurl.example.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.tbk.lnurl.simple.SimpleLnUrlAuth;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping(value = "/login")
@RequiredArgsConstructor
public class LnLoginPageCtrl {

    @NonNull
    private final HiddenServiceDefinition hiddenServiceDefinition;

    @SneakyThrows(URISyntaxException.class)
    private SimpleLnUrlAuth createNewLnUrlAuth() {
        String onionUrl = hiddenServiceDefinition.getVirtualHost()
                .map(val -> {
                    int port = hiddenServiceDefinition.getVirtualPort();
                    if (port == 80) {
                        return "http://" + val;
                    } else if (port == 443) {
                        return "https://" + val;
                    }
                    return "http://" + val + ":" + hiddenServiceDefinition.getVirtualPort();
                }).orElseThrow();

        URI callbackUrl = new URIBuilder(onionUrl)
                .setPath("/api/v1/lnauth/login")
                .build();

        return SimpleLnUrlAuth.create(callbackUrl);
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> loginHtml() {

        SimpleLnUrlAuth lnUrlAuth = createNewLnUrlAuth();
        String lnurl = lnUrlAuth.toLnUrl().toLnUrlString();

        UriComponents qrCodeImageUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .scheme(null)
                .host(null)
                .replacePath("/api/v1/lnauth/qrcode")
                .queryParam("lnurlauth", lnurl)
                .build();

        String template = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <title></title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<img src=\"%s\" alt=\"%s\" />\n"
                + "<p>\n"
                + "%s\n"
                + "</p>\n"
                + "</body>\n"
                + "</html>";

        String html = String.format(template, qrCodeImageUri.toUriString(), lnurl, lnurl);

        return ResponseEntity.ok(html);
    }
}
