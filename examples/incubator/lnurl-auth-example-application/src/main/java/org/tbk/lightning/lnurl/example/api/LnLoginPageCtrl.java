package org.tbk.lightning.lnurl.example.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig;
import org.tbk.lnurl.LnUrlAuth;
import org.tbk.lnurl.auth.LnurlAuthFactory;

import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping(value = "/login")
@RequiredArgsConstructor
public class LnLoginPageCtrl {

    @NonNull
    private final LnurlAuthFactory lnurlAuthFactory;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> loginHtml(HttpSession session) {
        LnUrlAuth lnUrlAuth = lnurlAuthFactory.createLnUrlAuth();

        session.setAttribute("k1", lnUrlAuth.getK1().getHex());

        UriComponents qrCodeImageUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .scheme(null)
                .host(null)
                .replacePath("/api/v1/lnauth/qrcode")
                .queryParam("lnurlauth", lnUrlAuth.toLnUrl().toLnUrlString())
                .build();

        return ResponseEntity.ok(createLoginPageHtml(qrCodeImageUri, lnUrlAuth));
    }

    private String createLoginPageHtml(UriComponents qrCodeImageUri, LnUrlAuth lnUrlAuth) {
        String lnurl = lnUrlAuth.toLnUrl().toLnUrlString();

        String template = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <title>lnurl-auth login</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<img src=\"%s\" alt=\"%s\" />\n"
                + "<p>\n"
                + "%s\n"
                + "</p>\n"
                + "<p>\n"
                + "k1: %s\n"
                + "</p>\n"
                + "<p>\n"
                + "logged in: %s\n"
                + "</p>\n"
                + "<p>\n"
                + "if ready click: <a href=\"%s\">%s</a>\n"
                + "</p>\n"
                + "</body>\n"
                + "</html>";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        return String.format(template, qrCodeImageUri.toUriString(),
                lnurl, lnurl, lnUrlAuth.getK1().getHex(), principal,
                LnurlAuthExampleApplicationSecurityConfig.lnurlAuthSessionLoginPath(),
                LnurlAuthExampleApplicationSecurityConfig.lnurlAuthSessionLoginPath());
    }
}
