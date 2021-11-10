package org.tbk.lnurl.simple;

import fr.acinq.bitcoin.Bech32;
import kotlin.Triple;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.Lnurl;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleLnurl implements Lnurl {
    private static final String HUMAN_READABLE_PART = "lnurl";

    public static SimpleLnurl fromBech32(String lnurl) {
        Triple<String, Byte[], Bech32.Encoding> decode = Bech32.decode(lnurl);
        String decodedLnUrl = new String(Bech32.five2eight(decode.component2(), 0), StandardCharsets.UTF_8);

        return fromUri(URI.create(decodedLnUrl));
    }

    public static SimpleLnurl fromUri(URI uri) {
        return new SimpleLnurl(uri);
    }

    public static String hrp() {
        return HUMAN_READABLE_PART;
    }

    private final URI decoded;

    transient String encoded;

    private SimpleLnurl(URI decoded) {
        this.decoded = requireNonNull(decoded);
    }

    @Override
    public URI toUri() {
        return this.decoded;
    }

    @Override
    public String toLnurlString() {
        if (this.encoded == null) {
            this.encoded = toBech32(this.decoded);
        }
        return this.encoded;
    }

    @Override
    public String toString() {
        return this.toLnurlString();
    }

    private static String toBech32(URI uri) {
        byte[] data = uri.toString().getBytes(StandardCharsets.UTF_8);
        return Bech32.encode(hrp(), toPrimitives(Bech32.eight2five(data)), Bech32.Encoding.Bech32);
    }

    private static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }
}
