package org.tbk.lnurl.simple;

import com.google.common.primitives.Bytes;
import fr.acinq.bitcoin.Bech32;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.LnUrl;
import scala.Tuple2;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

@Slf4j
@Value
public class SimpleLnUrl implements LnUrl {
    private static final String HUMAN_READABLE_PART = "lnurl";

    public static SimpleLnUrl decode(String lnurl) {
        return new SimpleLnUrl(lnurl);
    }

    public static SimpleLnUrl encode(URI uri) {
        byte[] data = uri.toString().getBytes(StandardCharsets.UTF_8);
        String lnurl = Bech32.encode(hrp(), eight2five(data));
        return decode(lnurl);
    }

    public static String hrp() {
        return HUMAN_READABLE_PART;
    }

    String encoded;

    URI decoded;

    private SimpleLnUrl(String lnurl) {
        this.encoded = requireNonNull(lnurl).toLowerCase();

        Tuple2<String, byte[]> decode = Bech32.decode(this.encoded);
        String decodedLnUrl = new String(Bech32.five2eight(decode._2), StandardCharsets.UTF_8);
        this.decoded = URI.create(decodedLnUrl);
    }

    @Override
    public URI toUri() {
        return decoded;
    }

    @Override
    public String toLnUrlString() {
        return this.encoded;
    }

    @Override
    public String toString() {
        return this.toLnUrlString();
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
