package org.tbk.spring.lnurl.security.ui;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class StaticTemplateUtils {

    static String readContents(String filename) {
        String file = StaticTemplateUtils.class.getPackage().getName().replace(".", "/") + "/" + filename;

        try (FastByteArrayOutputStream os = new FastByteArrayOutputStream();
             InputStream is = new ClassPathResource(file).getInputStream()) {
            is.transferTo(os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read contents of file", e);
        }
    }
}