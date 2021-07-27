package org.tbk.lightning.lnurl.example.api;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.lnurl.simple.SimpleLnUrl;
import org.tbk.lnurl.simple.SimpleLnUrlAuth;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.awt.image.BufferedImage;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnauth", produces = "application/json")
@RequiredArgsConstructor
public class LnUrlAuthApi {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();

    @NonNull
    private final HiddenServiceDefinition hiddenServiceDefinition;

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> loginImage(@RequestParam("lnurlauth") String lnurlauth) throws Exception {
        SimpleLnUrlAuth lnUrlAuth = SimpleLnUrlAuth.from(SimpleLnUrl.decode(lnurlauth));

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache()
                .noTransform()
                .mustRevalidate());

        return ResponseEntity.ok()
                .headers(headers)
                .body(generateQrCodeImage(lnUrlAuth));
    }

    /**
     * This method exists because different browsers use various "Accept" header values for image requests
     * and the extra path "/qrcode" solely exists to display an image (png) to every user no matter what browser.
     * Firefox it uses e.g. "image/webp, *\/*" as "Accept" header for images.
     * Chrome uses e.g. "image/avif,image/webp,image/apng,image/svg+xml,image/*,*\/*;q=0.8"
     */
    @GetMapping(path = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> lnurlAuthQrcode(@RequestParam("lnurlauth") String lnurlauth) throws Exception {
        return loginImage(lnurlauth);
    }

    private static BufferedImage generateQrCodeImage(SimpleLnUrlAuth lnUrlAuth) throws WriterException {
        return generateQrCodeImage(lnUrlAuth.toLnUrl().toLnUrlString());
    }

    private static BufferedImage generateQrCodeImage(String barcodeText) throws WriterException {
        BitMatrix bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
