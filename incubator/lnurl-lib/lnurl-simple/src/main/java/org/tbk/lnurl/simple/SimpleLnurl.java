package org.tbk.lnurl.simple;

import com.google.common.primitives.Bytes;
import fr.acinq.bitcoin.Bech32;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.Lnurl;
import scala.Tuple2;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleLnurl implements Lnurl {
    private static final String HUMAN_READABLE_PART = "lnurl";

    public static SimpleLnurl fromBech32(String lnurl) {
        Tuple2<String, byte[]> decode = Bech32.decode(lnurl);
        String decodedLnUrl = new String(Bech32.five2eight(decode._2), StandardCharsets.UTF_8);

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
    public String toLnUrlString() {
        if (this.encoded == null) {
            this.encoded = toBech32(this.decoded);
        }
        return this.encoded;
    }

    @Override
    public String toString() {
        return this.toLnUrlString();
    }

    private static String toBech32(URI uri) {
        byte[] data = uri.toString().getBytes(StandardCharsets.UTF_8);
        return Bech32.encode(hrp(), eight2five(data));
    }

    /**
     * This method is implemented here, as it is currently not
     * publicly visible in class {@link Bech32}.
     * Switch to an official version, once it is available.
     *
     * @param input a sequence of 8 bits integers
     * @return a sequence of 5 bits integers
     */
    private static byte[] eight2five(byte[] input) {
        long buffer = 0L;
        int count = 0;
        ArrayList<Byte> output = new ArrayList<>(input.length * 8 / 5 + 4);

        for (byte b : input) {
            buffer = (buffer << 8) | (b & 0xff);
            count = count + 8;
            while (count >= 5) {
                long l = (buffer >> (count - 5)) & 31;
                output.add(Long.valueOf(l).byteValue());
                count = count - 5;
            }
        }

        if (count > 0) {
            long l = (buffer << (5 - count)) & 31;
            output.add(Long.valueOf(l).byteValue());
        }

        return Bytes.toArray(output);
    }
}
