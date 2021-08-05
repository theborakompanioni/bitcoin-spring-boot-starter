package org.tbk.spring.lnurl.security.ui;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.util.FastByteArrayOutputStream;
import org.tbk.lnurl.Lnurl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

final class LnurlQrcodeUtils {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();
    private static final int DEFAULT_SIZE = 300;
    private static final Map<EncodeHintType, ?> defaultHints = ImmutableMap.<EncodeHintType, Object>builder()
            .put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
            .put(EncodeHintType.MARGIN, 0)
            .build();

    private LnurlQrcodeUtils() {
        throw new UnsupportedOperationException();
    }

    static byte[] imageToPng(BufferedImage image) throws IOException {
        return imageToBytes(image, "png");
    }

    private static byte[] imageToBytes(BufferedImage image, String formatName) throws IOException {
        // even a 200x200 png is ~1000 bytes - so use >1000 as default capacity for byte array
        try (FastByteArrayOutputStream os = new FastByteArrayOutputStream(1_024)) {
            ImageIO.write(image, formatName, os);
            return os.toByteArray();
        }
    }

    static BufferedImage generateQrCodeImage(Lnurl lnurl) throws WriterException {
        return generateQrCodeImage(lnurl, DEFAULT_SIZE);
    }

    static BufferedImage generateQrCodeImage(Lnurl lnurl, int size) throws WriterException {
        return generateQrCodeImage(lnurl, size, size);
    }

    private static BufferedImage generateQrCodeImage(Lnurl lnurl, int width, int height) throws WriterException {
        return generateQrCodeImage(lnurl.toLnurlString(), width, height);
    }

    private static BufferedImage generateQrCodeImage(String barcodeText, int width, int height) throws WriterException {
        BitMatrix bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height, defaultHints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
