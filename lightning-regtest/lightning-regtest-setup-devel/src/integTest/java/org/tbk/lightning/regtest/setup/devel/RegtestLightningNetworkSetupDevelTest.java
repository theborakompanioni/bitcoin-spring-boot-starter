package org.tbk.lightning.regtest.setup.devel;

import com.google.common.base.Stopwatch;
import com.google.protobuf.ByteString;
import fr.acinq.lightning.MilliSatoshi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;
import org.tbk.lightning.client.common.core.LightningCommonClient;
import org.tbk.lightning.client.common.core.proto.*;
import org.tbk.lightning.client.common.core.proto.CommonLookupInvoiceResponse.InvoiceStatus;
import org.tbk.lightning.client.common.core.proto.CommonPayResponse.PaymentStatus;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import org.tbk.lightning.regtest.core.LightningNetworkConstants;
import org.tbk.lightning.regtest.setup.RegtestLightningNetworkSetup;
import org.tbk.lightning.regtest.setup.devel.impl.LocalRegtestLightningNetworkSetupConfig;
import org.tbk.lightning.regtest.setup.util.ClnRouteVerifier;
import org.tbk.lightning.regtest.setup.util.SimpleClnRouteVerifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = LocalRegtestLightningNetworkSetupConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class RegtestLightningNetworkSetupDevelTest {

    private static String randomLabel() {
        return LocalDateTime.now(ZoneOffset.UTC).toString();
    }

    private static final ClnRouteVerifier routeVerifier = new SimpleClnRouteVerifier();

    // autowiring the setup verifies it finished without errors
    @Autowired
    private RegtestLightningNetworkSetup setupFinished;

    @Autowired
    @Qualifier("nodeAppLightningCommonClient")
    private LightningCommonClient<NodeGrpc.NodeBlockingStub> appNode;

    // If the app node is not directly connected to this node,
    // make sure that the channel graph is already synced, and
    // the app node is aware of a path to this node!
    @Autowired
    @Qualifier("nodeCharlieLightningCommonClient")
    private LightningCommonClient<NodeGrpc.NodeBlockingStub> userNode;

    @BeforeEach
    void setUp() {
        // verify nodes are connected via payment path
        Assert.isTrue(routeVerifier.hasDirectRoute(appNode, userNode), "Sanity check failed: No route between nodes");
    }

    @Test
    void itShouldVerifyPaymentCanBeSentOverChannelSuccessfully() {
        MilliSatoshi millisats = new MilliSatoshi(1_000L);

        // generate an invoice on user node
        String userInvoiceLabel = randomLabel();
        Duration expiry = Duration.ofSeconds(30);
        CommonCreateInvoiceResponse invoiceResponse = requireNonNull(userNode.createInvoice(CommonCreateInvoiceRequest.newBuilder()
                .setLabel(userInvoiceLabel)
                .setExpiry(expiry.toSeconds())
                .setAmountMsat(millisats.getMsat())
                .build()).block(Duration.ofSeconds(30)));

        assertThat(invoiceResponse.getPaymentRequest()).startsWith("lnbcrt1");

        Stopwatch sw = Stopwatch.createStarted();

        // pay invoice from app node
        CommonPayResponse cln1PayResponse = requireNonNull(appNode.pay(CommonPayRequest.newBuilder()
                        .setPaymentRequest(invoiceResponse.getPaymentRequest())
                        .build())
                .block(Duration.ofSeconds(30)));

        assertThat(cln1PayResponse.getStatus()).isEqualTo(PaymentStatus.COMPLETE);
        assertThat(cln1PayResponse.getPaymentHash()).isEqualTo(invoiceResponse.getPaymentHash());
        assertThat(cln1PayResponse.getPaymentPreimage()).isNotIn(null, ByteString.EMPTY);

        log.info("Payment sent on Node 1 after {}", sw);

        CommonLookupInvoiceRequest request = CommonLookupInvoiceRequest.newBuilder()
                .setPaymentHash(invoiceResponse.getPaymentHash())
                .build();

        CommonLookupInvoiceResponse userClnInvoice = Flux.interval(Duration.ZERO, Duration.ofSeconds(1L))
                .flatMap(it -> userNode.lookupInvoice(request))
                .flatMap(Mono::justOrEmpty)
                .filter(it -> it.getStatus() == InvoiceStatus.COMPLETE)
                .blockFirst(expiry.plus(Duration.ofSeconds(1)));

        assertThat(userClnInvoice).isNotNull();
        assertThat(userClnInvoice.getPaymentHash()).isEqualTo(cln1PayResponse.getPaymentHash());
        assertThat(userClnInvoice.getPaymentPreimage()).isEqualTo(cln1PayResponse.getPaymentPreimage());
        assertThat(userClnInvoice.getAmountMsat()).isEqualTo(cln1PayResponse.getAmountMsat());
        assertThat(userClnInvoice.getStatus()).isEqualTo(InvoiceStatus.COMPLETE);

        log.info("Payment received on Node 2 after {}", sw.stop());
    }

    /**
     * **NOTE**: This test should give you a feeling about how nodes (especially CLN) will behave in production.
     * <p>
     * It can take a long time till the state of a failed payment switches from PENDING to FAILED.
     * Verify that it will indeed happen and provide the waiting time as log output.
     * Depending on your node implementation, this can take <i>several</i> minutes!
     */
    @Test
    void itShouldVerifyFailedPaymentsCanBeListed() {
        // generate an invoice that cannot be paid
        MilliSatoshi nonPayableAmount = LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.plus(new MilliSatoshi(1));

        // generate an non-payable invoice on user node
        Duration expiry = Duration.ofMinutes(60);
        CommonCreateInvoiceResponse invoiceResponse = requireNonNull(userNode.createInvoice(CommonCreateInvoiceRequest.newBuilder()
                .setLabel(randomLabel())
                .setExpiry(expiry.toSeconds())
                .setAmountMsat(nonPayableAmount.getMsat())
                .build()).block(Duration.ofSeconds(30)));

        String unaffordableInvoice = invoiceResponse.getPaymentRequest();
        assertThat(unaffordableInvoice).startsWith("lnbcrt1");

        Duration timeout = Duration.ofSeconds(5);
        try {
            CommonPayResponse ignoredOnPurpose = appNode.pay(CommonPayRequest.newBuilder()
                            .setPaymentRequest(unaffordableInvoice)
                            .setTimeoutSeconds(Math.toIntExact(timeout.toSeconds()))
                            .build())
                    .block(timeout.plus(Duration.ofSeconds(10)));

            Assertions.fail("Should have thrown exception");
        } catch (Exception e) {
            // empty on purpose
        }

        CommonLookupPaymentRequest lookupPaymentRequest = CommonLookupPaymentRequest.newBuilder()
                .setPaymentHash(invoiceResponse.getPaymentHash())
                .build();

        Stopwatch sw = Stopwatch.createStarted();
        PaymentStatus initialStatus = Flux.interval(Duration.ZERO, Duration.ofSeconds(2L))
                .flatMap(it -> appNode.lookupPayment(lookupPaymentRequest))
                .doOnNext(it -> log.info("{}", it))
                .map(CommonLookupPaymentResponse::getStatus)
                .blockFirst(Duration.ofSeconds(10));

        log.info("Payment was included in `listPays` response after {}", sw);

        // payment might at first still be reported as PENDING
        assertThat(initialStatus).isIn(PaymentStatus.PENDING, PaymentStatus.FAILED);

        // wait till payment state is reported as FAILED
        PaymentStatus failedState = Flux.interval(Duration.ZERO, Duration.ofSeconds(2L))
                .flatMap(it -> appNode.lookupPayment(lookupPaymentRequest))
                .map(CommonLookupPaymentResponse::getStatus)
                .filter(it -> it == PaymentStatus.FAILED)
                .blockFirst(expiry.plus(Duration.ofSeconds(1)));

        log.info("Payment switched from PENDING to FAILED after {}", sw.stop());

        assertThat(failedState).isEqualTo(PaymentStatus.FAILED);
    }
}