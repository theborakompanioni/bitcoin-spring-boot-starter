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
import org.tbk.lightning.client.common.core.proto.CommonCreateInvoiceRequest;
import org.tbk.lightning.client.common.core.proto.CommonCreateInvoiceResponse;
import org.tbk.lightning.cln.grpc.client.*;
import org.tbk.lightning.cln.grpc.client.ListpaysPays.ListpaysPaysStatus;
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

        // generate an invoice on Node 2
        // see: https://lightning.readthedocs.io/lightning-invoice.7.html
        String userInvoiceLabel = randomLabel();
        Duration expiry = Duration.ofSeconds(30);
        CommonCreateInvoiceResponse invoiceResponse = requireNonNull(userNode.createInvoice(CommonCreateInvoiceRequest.newBuilder()
                .setLabel(userInvoiceLabel)
                .setExpiry(expiry.toSeconds())
                .setAmountMsat(millisats.getMsat())
                .build()).block(Duration.ofSeconds(30)));

        assertThat(invoiceResponse.getPaymentRequest()).startsWith("lnbcrt1");

        Stopwatch sw = Stopwatch.createStarted();

        // pay invoice from Node 1
        // see: https://lightning.readthedocs.io/lightning-pay.7.html
        PayResponse cln1PayResponse = appNode.baseClient().pay(PayRequest.newBuilder()
                .setBolt11(invoiceResponse.getPaymentRequest())
                .build());

        assertThat(cln1PayResponse.getAmountMsat().getMsat()).isEqualTo(millisats.getMsat());
        assertThat(cln1PayResponse.getStatus()).isEqualTo(PayResponse.PayStatus.COMPLETE);
        assertThat(cln1PayResponse.getPaymentHash()).isEqualTo(invoiceResponse.getPaymentHash());

        log.info("Payment sent on Node 1 after {}", sw);

        ListinvoicesRequest request = ListinvoicesRequest.newBuilder()
                .setPaymentHash(ByteString.copyFrom(invoiceResponse.getPaymentHash().toByteArray()))
                .build();

        ListinvoicesInvoices userClnInvoices = Flux.interval(Duration.ZERO, Duration.ofSeconds(1L))
                .flatMap(it -> Mono.fromCallable(() -> userNode.baseClient().listInvoices(request)))
                .filter(it -> it.getInvoicesCount() > 0)
                .map(it -> it.getInvoicesList().stream()
                        .filter(ListinvoicesInvoices::hasBolt11)
                        .filter(invoice -> invoice.getBolt11().equals(invoiceResponse.getPaymentRequest()))
                        .findFirst())
                .flatMap(Mono::justOrEmpty)
                .filter(it -> it.getStatus() == ListinvoicesInvoices.ListinvoicesInvoicesStatus.PAID)
                .blockFirst(expiry.plus(Duration.ofSeconds(1)));

        assertThat(userClnInvoices).isNotNull();
        assertThat(userClnInvoices.getAmountMsat()).isNotNull();
        assertThat(userClnInvoices.getAmountMsat().getMsat()).isEqualTo(millisats.getMsat());
        assertThat(userClnInvoices.getLabel()).isEqualTo(userInvoiceLabel);

        log.info("Payment received on Node 2 after {}", sw.stop());
    }

    /**
     * **NOTE**: This test should give you a feeling about how CLN will behave in production.
     * <p>
     * It can take a long time till the state of a failed payment switches from PENDING to FAILED.
     * Verify that it will indeed happen and provide the waiting time as log output. This can take SEVERAL minutes!
     */
    @Test
    void itShouldVerifyFailedPaymentsCanBeListed() {
        // generate an invoice that cannot be paid
        MilliSatoshi nonPayableAmount = LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT.plus(new MilliSatoshi(1));

        // generate an non-payable invoice on Node 2
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
            PayResponse ignoredOnPurpose = appNode.baseClient().pay(PayRequest.newBuilder()
                    .setBolt11(unaffordableInvoice)
                    .setRetryFor(Math.toIntExact(timeout.toSeconds()))
                    .build());

            Assertions.fail("Should have thrown exception");
        } catch (Exception e) {
            // empty on purpose
        }

        ListpaysRequest listpaysRequest = ListpaysRequest.newBuilder()
                .setBolt11(unaffordableInvoice)
                .build();

        Stopwatch sw = Stopwatch.createStarted();
        ListpaysPaysStatus initialStatus = Flux.interval(Duration.ZERO, Duration.ofSeconds(2L))
                .map(it -> appNode.baseClient().listPays(listpaysRequest))
                .doOnNext(it -> log.info("{}", it))
                .filter(it -> it.getPaysCount() > 0)
                .map(it -> it.getPays(0).getStatus())
                .blockFirst(Duration.ofSeconds(10));

        log.info("Payment was included in `listPays` response after {}", sw);

        // payment might at first still be reported as PENDING
        assertThat(initialStatus).isIn(ListpaysPaysStatus.PENDING, ListpaysPaysStatus.FAILED);

        // wait till payment state is reported as FAILED
        ListpaysPaysStatus failedState = Flux.interval(Duration.ZERO, Duration.ofSeconds(2L))
                .map(it -> appNode.baseClient().listPays(listpaysRequest).getPays(0).getStatus())
                .filter(it -> it == ListpaysPaysStatus.FAILED)
                .blockFirst(expiry.plus(Duration.ofSeconds(1)));

        log.info("Payment switched from PENDING to FAILED after {}", sw.stop());

        assertThat(failedState).isEqualTo(ListpaysPaysStatus.FAILED);
    }
}