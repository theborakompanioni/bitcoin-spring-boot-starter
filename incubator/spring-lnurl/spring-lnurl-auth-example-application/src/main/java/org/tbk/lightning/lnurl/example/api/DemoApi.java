package org.tbk.lightning.lnurl.example.api;

import fr.acinq.bitcoin.Base58;
import fr.acinq.bitcoin.Crypto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoApi {
    private static final String ANONYMOUS_NAME = "anonymous";

    @GetMapping(path = "/image/avatar/anonymous", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> avatarAnon() {
        String url = String.format("https://robohash.org/%s.png", ANONYMOUS_NAME);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping(path = "/image/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> avatar(@Nullable Principal principal) {
        String nameHashed = toUsername(principal);

        String url = String.format("https://robohash.org/%s.png", UriUtils.encodePath(nameHashed, StandardCharsets.UTF_8));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping(path = "/image/name", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> get(@Nullable Principal principal) {
        String nameHashed = toUsername(principal);

        return ResponseEntity.ok(createImageWithText("@" + nameHashed));
    }

    private static String toUsername(Principal principal) {
        return Optional.ofNullable(principal)
                .map(Principal::getName)
                .map(it -> principal.getName().getBytes(StandardCharsets.UTF_8))
                .map(Crypto::sha256)
                .map(Base58::encode)
                .map(it -> it.substring(0, 30))
                .orElse(ANONYMOUS_NAME);
    }

    private static BufferedImage createImageWithText(String text) {
        BufferedImage bufferedImage = new BufferedImage(300, 30, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();

        g.drawString(text, 20, 20);

        return bufferedImage;
    }
}
