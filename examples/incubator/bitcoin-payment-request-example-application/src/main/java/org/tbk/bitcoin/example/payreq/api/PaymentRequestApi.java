package org.tbk.bitcoin.example.payreq.api;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.tbk.bitcoin.example.payreq.api.query.PaymentRequestQueryParams;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Map;

import static org.tbk.bitcoin.example.payreq.api.PaymentRequestUrlHelper.toPaymentUrl;

@RestController
@RequestMapping("/api/v1/payment/request")
public class PaymentRequestApi {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> paymentRequestJson(@Validated PaymentRequestQueryParams paymentRequestQueryParams) {
        String paymentUrl = toPaymentUrl(paymentRequestQueryParams);

        return ResponseEntity.ok(ImmutableMap.<String, String>builder()
                .put("paymentUrl", paymentUrl)
                .build());
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> paymentRequestHtml(@Validated PaymentRequestQueryParams paymentRequestQueryParams) {
        String paymentUrl = toPaymentUrl(paymentRequestQueryParams);

        UriComponents qrCodeImageUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .scheme(null)
                .host(null)
                .pathSegment("qrcode")
                .build();

        String template = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "    <title></title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<img src=\"%s\" alt=\"%s\" />\n" +
                "</body>\n" +
                "</html>";

        String html = String.format(template, qrCodeImageUri.toUriString(), paymentUrl);

        return ResponseEntity.ok(html);
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> paymentRequestText(@Validated PaymentRequestQueryParams paymentRequestQueryParams) {
        String paymentUrl = toPaymentUrl(paymentRequestQueryParams);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> paymentRequestImage(@Validated PaymentRequestQueryParams paymentRequestQueryParams) throws Exception {
        String paymentUrl = toPaymentUrl(paymentRequestQueryParams);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(365))
                .noTransform()
                .mustRevalidate());

        return ResponseEntity.ok()
                .headers(headers)
                .body(generateQrCodeImage(paymentUrl));
    }

    /**
     * This method exists because different browsers use various "Accept" header values for image requests
     * and the extra path "/qrcode" solely exists to display an image (png) to every user no matter what browser.
     * Firefox it uses e.g. "image/webp, *\/*" as "Accept" header for images.
     * Chrome uses e.g. "image/avif,image/webp,image/apng,image/svg+xml,image/*,*\/*;q=0.8"
     */
    @GetMapping(path = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> paymentRequestQrcode(@Validated PaymentRequestQueryParams paymentRequestQueryParams) throws Exception {
        return paymentRequestImage(paymentRequestQueryParams);
    }

    private static BufferedImage generateQrCodeImage(String barcodeText) throws WriterException {
        BitMatrix bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}
