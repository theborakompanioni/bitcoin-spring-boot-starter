package org.tbk.lightning.playground.example.util;

import fr.acinq.bitcoin.Chain;
import fr.acinq.lightning.payment.Bolt11Invoice;
import fr.acinq.lightning.payment.Bolt12Invoice;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceUtilsTest {

    // invoice taken from https://www.bolt11.org/
    private static final String bolt11Invoice = "lnbc15u1p3xnhl2pp5jptserfk3zk4qy42tlucycrfwxhydvlemu9pqr93tuzlv9cc7g3sdqsvfhkcap3xyhx7un8cqzpgxqzjcsp5f8c52y2stc300gl6s4xswtjpc37hrnnr3c9wvtgjfuvqmpm35evq9qyyssqy4lgd8tj637qcjp05rdpxxykjenthxftej7a2zzmwrmrl70fyj9hvj0rewhzj7jfyuwkwcg9g2jpwtk3wkjtwnkdks84hsnu8xps5vsq4gj5hs";
    private static final String bolt11InvoiceWithRoutingHints = "lnbc210n1p3c0qn8sp5m2wy7qc2y3sq2j8lmcdde8z5jzpjxhnks37ntwtl2kq63rj0apmspp5jzwghepfddyryjzlm2ac7gtdrngkq85fmczjdxek9p6tl7du7j2qdq2f38xy6t5wvxqyjw5qcqpjrzjq0y2htc5v6hja5ly74ltgy7y0386za75dpvnxcsdlhr3wayhjh07qzavygqqpjqqqqqqqqlgqqqqqqgq9q9qyysgqccjqlhlfgkqr0nt82h4se56nteylw3tvg20thqkxyrrccy4g2tn4zq7pjnjyr330vajtl32uahcaywky6ale4ws0vfke8ges7aynvmgqgmhy53";


    // invoice taken from https://github.com/ACINQ/lightning-kmp/blob/master/src/commonTest/kotlin/fr/acinq/lightning/payment/Bolt12InvoiceTestsCommon.kt#L535
    private static final String bolt12Invoice = "lni1qqgds4gweqxey37gexf5jus4kcrwuq3qqc3xu3s3rg94nj40zfsy866mhu5vxne6tcej5878k2mneuvgjy8s5predakx793pqfxv2rtqfajhp98c5tlsxxkkmzy0ntpzp2rtt9yum2495hqrq4wkj5pqqc3xu3s3rg94nj40zfsy866mhu5vxne6tcej5878k2mneuvgjy84yqucj6q9sggrnl24r93kfmdnatwpy72mxg7ygr9waxu0830kkpqx84pd5j65fhg2pxqzfnzs6cz0v4cff79zlup344kc3ru6cgs2s66ef8x64fd9cqc9t45s954fef6n3ql8urpc4r2vvunc0uv9yq37g485heph6lpuw34ywxadqypwq3hlcrpyk32zdvlrgfsdnx5jegumenll49v502862l9sq5erz3qqxte8tyk308ykd6fqy2lxkrsmeq77d8s5977pzmc68lgvs2xcn0kfvnlzud9fvkv900ggwe7yf9hf7lr6qz3pcqqqqqqqqqqqqqqq5qqqqqqqqqqqqqwjfvkl43fqqqqqqzjqgcuhrdv2sgq5spd8qp4ev2rw0v9r7cvvrntlzpvlwmd8vczycklu87336h55g24q8xykszczzqjvc5xkqnm9wz203ghlqvdddkyglxkzyz5xkk2fek42tfwqxp2ad8cypv26x5zxkyk675ep3v48grwydze6nvvg56cklgmvztuny58t5j0fl3hemx3lvd0ryx89jtf0h069z6r2qwqvjlyrewvzsfqmmfajs70q";

    @Test
    void itShouldFailDecodingInvalidInvoice() {
        assertThrows(IllegalStateException.class, () -> {
            InvoiceUtils.decodeInvoice("invalid value")
                    .blockOptional()
                    .orElseThrow();
        }, "Failed to decode invoice.");
    }

    @Test
    void itShouldDecodeBolt11InvoiceSuccessfully() {
        Bolt11Invoice decodedInvoice = InvoiceUtils.decodeBolt11Invoice(bolt11Invoice)
                .blockOptional()
                .orElseThrow();

        assertThat(decodedInvoice, is(notNullValue()));
        assertThat(decodedInvoice.getPaymentHash().toHex(), is("90570c8d3688ad5012aa5ff982606971ae46b3f9df0a100cb15f05f61718f223"));
        assertThat(decodedInvoice.getPaymentSecret().toHex(), is("49f14511505e22f7a3fa854d072e41c47d71ce638e0ae62d124f180d8771a658"));
        assertThat(decodedInvoice.getPaymentMetadata(), is(nullValue()));
        assertThat(HexFormat.of().formatHex(decodedInvoice.getFeatures().toByteArray()), is("024200"));
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
    void itShouldDecodeBolt11InvoiceWithRoutingHintsSuccessfully() {
        Bolt11Invoice decodedInvoice = InvoiceUtils.decodeBolt11Invoice(bolt11InvoiceWithRoutingHints)
                .blockOptional()
                .orElseThrow();

        assertThat(decodedInvoice, is(notNullValue()));
        assertThat(decodedInvoice.getPaymentHash().toHex(), is("909c8be4296b4832485fdabb8f216d1cd1601e89de05269b362874bff9bcf494"));
        assertThat(decodedInvoice.getPaymentSecret().toHex(), is("da9c4f030a24600548ffde1adc9c549083235e76847d35b97f5581a88e4fe877"));
        assertThat(decodedInvoice.getPaymentMetadata(), is(nullValue()));
        assertThat(HexFormat.of().formatHex(decodedInvoice.getFeatures().toByteArray()), is("024100"));
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

        decodedInvoice.getRoutingInfo().stream().findFirst().orElseThrow();

        Bolt11Invoice.TaggedField.RoutingInfo routingInfo = decodedInvoice.getRoutingInfo().stream().findFirst().orElseThrow();
        assertThat(routingInfo.getHints(), hasSize(1));

        Bolt11Invoice.TaggedField.ExtraHop extraHop = routingInfo.getHints().stream().findFirst().orElseThrow();
        assertThat(extraHop.getNodeId().toHex(), is("03c8abaf1466af2ed3e4f57eb413c47c4fa177d4685933620dfdc717749795dfe0"));
        assertThat(extraHop.getShortChannelId().toString(), is("764962x200x0"));
        assertThat(extraHop.getFeeBase().getMsat(), is(1_000L));
        assertThat(extraHop.getFeeProportionalMillionths(), is(1L));
        assertThat(extraHop.getCltvExpiryDelta().toLong(), is(40L));
    }

    @Test
    void itShouldDecodeBolt12InvoiceSuccessfully() {
        Bolt12Invoice decodedInvoice = InvoiceUtils.decodeBolt12Invoice(bolt12Invoice)
                .blockOptional()
                .orElseThrow();

        assertThat(decodedInvoice, is(notNullValue()));
        assertThat(decodedInvoice.getPaymentHash().toHex(), is("14805a7006b96286e7b0a3f618c1cd7f1059f76da766044c5bfc3fa31d5e9442"));
        assertThat(HexFormat.of().formatHex(decodedInvoice.getFeatures().toByteArray()), is(""));
        assertThat(decodedInvoice.getNodeId().toHex(), is("024cc50d604f657094f8a2ff031ad6d888f9ac220a86b5949cdaaa5a5c03055d69"));
        assertThat(decodedInvoice.getAmount().getMsat(), is(10_000_000L));
        assertThat(decodedInvoice.getDescription(), is("yolo"));
        assertThat(decodedInvoice.getInvoiceRequest(), is(notNullValue()));
        assertThat(decodedInvoice.getInvoiceRequest().getChain(), is(Chain.Regtest.INSTANCE.getChainHash()));
        assertThat(decodedInvoice.getInvoiceRequest().getAmount().getMsat(), is(10_000_000L));
        assertThat(decodedInvoice.getInvoiceRequest().getPayerId().toHex(), is("039fd55196364edb3eadc12795b323c440caee9b8f3c5f6b04063d42da4b544dd0"));
        assertThat(decodedInvoice.getInvoiceRequest().getMetadata().toHex(), is("d8550ec80d9247c8c993497215b606ee"));
        assertThat(decodedInvoice.getInvoiceRequest().getOffer().getOfferId().toHex(), is("623e0c718f2a5292b5dbe05e6cc7097aac856d4394a3d573d9f7896892878503"));
        assertThat(decodedInvoice.getRelativeExpirySeconds(), is(7200L));
        assertThat(decodedInvoice.getSignature().toHex(), is("b15a35046b12daf53218b2a740dc468b3a9b188a6b16fa36c12f93250eba49e9fc6f9d9a3f635e3218e592d2fbbf451686a0380c97c83cb9828241bda7b2879e"));
    }
}
