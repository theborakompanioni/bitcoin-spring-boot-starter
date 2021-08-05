package org.tbk.spring.lnurl.security.ui;

import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.Lnurl;
import org.tbk.lnurl.auth.LnurlAuth;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
final class LnurlQrcode implements Lnurl {
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    private final Lnurl lnurl;
    private final int size;

    LnurlQrcode(Lnurl lnurl, int size) {
        checkArgument(size > 0, "'size' must be a positive integer");
        this.lnurl = requireNonNull(lnurl);
        this.size = size;
    }

    @Override
    public URI toUri() {
        return lnurl.toUri();
    }

    @Override
    public String toLnurlString() {
        return lnurl.toLnurlString();
    }

    public String createHtmlImageTagOrFallbackTag() {
        try {
            return createHtmlImageTag();
        } catch (IOException e) {
            log.warn("Could not write lnurl-auth qr code on default login page. Will use fallback to text! Error: {}", e.getMessage());
            return createFallbackTag();
        }
    }

    public String createHtmlImageTag() throws IOException {
        String encoding = createDataImageTagValue();
        String alt = this.toLnurlString();

        return String.format("<img src=\"%s\" alt=\"%s\" height=\"%d\" width=\"%d\" />", encoding, alt, size, size);
    }

    private String createFallbackTag() {
        return String.format("<div>%s</div>", this.toLnurlString());
    }

    private String createDataImageTagValue() throws IOException {
        BufferedImage image = createQrCodeImage();
        byte[] imageBytes = LnurlQrcodeUtils.imageToPng(image);

        return String.format("data:image/png;base64,%s", base64Encoder.encodeToString(imageBytes));
    }

    private BufferedImage createQrCodeImage() throws IOException {
        try {
            return LnurlQrcodeUtils.generateQrCodeImage(this, this.size);
        } catch (WriterException e) {
            throw new IOException("Error creating qr code from lnurl", e);
        }
    }
}
