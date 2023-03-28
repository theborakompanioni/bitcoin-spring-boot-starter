package org.tbk.lightning.playground.example.util;

import fr.acinq.lightning.payment.PaymentRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceUtilsTest {

    // invoice taken from https://www.bolt11.org/
    private static final String bolt11Invoice = "lnbc15u1p3xnhl2pp5jptserfk3zk4qy42tlucycrfwxhydvlemu9pqr93tuzlv9cc7g3sdqsvfhkcap3xyhx7un8cqzpgxqzjcsp5f8c52y2stc300gl6s4xswtjpc37hrnnr3c9wvtgjfuvqmpm35evq9qyyssqy4lgd8tj637qcjp05rdpxxykjenthxftej7a2zzmwrmrl70fyj9hvj0rewhzj7jfyuwkwcg9g2jpwtk3wkjtwnkdks84hsnu8xps5vsq4gj5hs";
    private static final String bolt11InvoiceWithRoutingHints = "lnbc210n1p3c0qn8sp5m2wy7qc2y3sq2j8lmcdde8z5jzpjxhnks37ntwtl2kq63rj0apmspp5jzwghepfddyryjzlm2ac7gtdrngkq85fmczjdxek9p6tl7du7j2qdq2f38xy6t5wvxqyjw5qcqpjrzjq0y2htc5v6hja5ly74ltgy7y0386za75dpvnxcsdlhr3wayhjh07qzavygqqpjqqqqqqqqlgqqqqqqgq9q9qyysgqccjqlhlfgkqr0nt82h4se56nteylw3tvg20thqkxyrrccy4g2tn4zq7pjnjyr330vajtl32uahcaywky6ale4ws0vfke8ges7aynvmgqgmhy53";


    @Test
    void itShouldFailDecodingInvalidInvoice() {
        assertThrows(IllegalStateException.class, () -> {
            InvoiceUtils.decodeInvoice("invalid value")
                    .blockOptional()
                    .orElseThrow();
        }, "Failed to decode invoice.");
    }

    @Test
    void itShouldDecodeInvoiceSuccessfully() {
        PaymentRequest decodedInvoice = InvoiceUtils.decodeInvoice(bolt11Invoice)
                .blockOptional()
                .orElseThrow();

        assertThat(decodedInvoice, is(notNullValue()));
        assertThat(decodedInvoice.getPaymentHash().toHex(), is("90570c8d3688ad5012aa5ff982606971ae46b3f9df0a100cb15f05f61718f223"));
        assertThat(decodedInvoice.getPaymentSecret().toHex(), is("49f14511505e22f7a3fa854d072e41c47d71ce638e0ae62d124f180d8771a658"));
        assertThat(decodedInvoice.getPaymentMetadata(), is(nullValue()));
        assertThat(decodedInvoice.getFeatures().toHex(), is("024200"));
        assertThat(decodedInvoice.getNodeId().toHex(), is("03d6b14390cd178d670aa2d57c93d9519feaae7d1e34264d8bbb7932d47b75a50d"));
        assertThat(decodedInvoice.getAmount().getMsat(), is(1_500_000L));
        assertThat(decodedInvoice.getDescription(), is("bolt11.org"));
        assertThat(decodedInvoice.getDescriptionHash(), is(nullValue()));
        assertThat(decodedInvoice.getTimestampSeconds(), is(1651105770L));
        assertThat(decodedInvoice.getExpirySeconds(), is(600L));
        assertThat(decodedInvoice.getMinFinalExpiryDelta().toLong(), is(40L));
        assertThat(decodedInvoice.getFallbackAddress(), is(nullValue()));
        assertThat(decodedInvoice.getSignature().toHex(), is("257e869d72d47c0c482fa0da1318969666bb992bccbdd5085b70f63ff9e9248b7649e3cbae297a49271d67610542a4172ed175a4b74ecdb40f5bc27c39830a3200"));
        assertThat(decodedInvoice.getRoutingInfo(), hasSize(0));
    }

    @Test
    void itShouldDecodeInvoiceWithRoutingHintsSuccessfully() {
        PaymentRequest decodedInvoice = InvoiceUtils.decodeInvoice(bolt11InvoiceWithRoutingHints)
                .blockOptional()
                .orElseThrow();

        assertThat(decodedInvoice, is(notNullValue()));
        assertThat(decodedInvoice.getPaymentHash().toHex(), is("909c8be4296b4832485fdabb8f216d1cd1601e89de05269b362874bff9bcf494"));
        assertThat(decodedInvoice.getPaymentSecret().toHex(), is("da9c4f030a24600548ffde1adc9c549083235e76847d35b97f5581a88e4fe877"));
        assertThat(decodedInvoice.getPaymentMetadata(), is(nullValue()));
        assertThat(decodedInvoice.getFeatures().toHex(), is("024100"));
        assertThat(decodedInvoice.getNodeId().toHex(), is("022bd0aa893db4ac890e457cca8c83f112518d6941bf9153dab4bf904620503a78"));
        assertThat(decodedInvoice.getAmount().getMsat(), is(21_000L));
        assertThat(decodedInvoice.getDescription(), is("LNbits"));
        assertThat(decodedInvoice.getDescriptionHash(), is(nullValue()));
        assertThat(decodedInvoice.getTimestampSeconds(), is(1669825127L));
        assertThat(decodedInvoice.getExpirySeconds(), is(604_800L));
        assertThat(decodedInvoice.getMinFinalExpiryDelta().toLong(), is(18L));
        assertThat(decodedInvoice.getFallbackAddress(), is(nullValue()));
        assertThat(decodedInvoice.getSignature().toHex(), is("c6240fdfe9458037cd6755eb0cd3535e49f7456c429ebb82c620c78c12a852e75103c194e441c62f6764bfc55cedf1d23ac4d77f9aba0f626d93a330f749366d00"));
        assertThat(decodedInvoice.getRoutingInfo(), hasSize(1));

        PaymentRequest.TaggedField.RoutingInfo routingInfo = decodedInvoice.getRoutingInfo().stream().findFirst().orElseThrow();
        assertThat(routingInfo.getHints(), hasSize(1));

        PaymentRequest.TaggedField.ExtraHop extraHop = routingInfo.getHints().stream().findFirst().orElseThrow();
        assertThat(extraHop.getNodeId().toHex(), is("03c8abaf1466af2ed3e4f57eb413c47c4fa177d4685933620dfdc717749795dfe0"));
        assertThat(extraHop.getShortChannelId().toString(), is("764962x200x0"));
        assertThat(extraHop.getFeeBase().getMsat(), is(1_000L));
        assertThat(extraHop.getFeeProportionalMillionths(), is(1L));
        assertThat(extraHop.getCltvExpiryDelta().toLong(), is(40L));
    }
}
