package org.tbk.lightning.playground.example.invoice;

import fr.acinq.bitcoin.ByteVector;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.lightning.CltvExpiryDelta;
import fr.acinq.lightning.MilliSatoshi;
import fr.acinq.lightning.payment.PaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tbk.lightning.playground.example.api.ErrorAttributesSchema;
import org.tbk.lightning.playground.example.invoice.dto.DecodeInvoiceRequestDto;
import org.tbk.lightning.playground.example.invoice.dto.DecodeInvoiceResponseDto;
import org.tbk.lightning.playground.example.util.InvoiceUtils;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/ln/invoice",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
@Tags({
        @Tag(name = "ln")
})
@ApiResponses({
        @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "403", content = @Content),
})
public class LnInvoiceApi {

    @Operation(
            summary = "Decode bolt11 invoices"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorAttributesSchema.class))),
    })
    @PostMapping(path = "/decode")
    public ResponseEntity<DecodeInvoiceResponseDto> decodeInvoice(@RequestBody DecodeInvoiceRequestDto body) {
        PaymentRequest decodedInvoice = InvoiceUtils.decodeInvoice(body.getInvoice())
                .onErrorComplete()
                .blockOptional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to decode invoice."));

        return ResponseEntity.ok().body(DecodeInvoiceResponseDto.builder()
                .pubkey(decodedInvoice.getNodeId().toHex())
                .paymentHash(decodedInvoice.getPaymentHash().toHex())
                .paymentSecret(decodedInvoice.getPaymentSecret().toHex())
                .paymentMetadata(Optional.ofNullable(decodedInvoice.getPaymentMetadata())
                        .map(ByteVector::toHex)
                        .orElse(null))
                .amountMsat(Optional.ofNullable(decodedInvoice.getAmount())
                        .map(MilliSatoshi::getMsat)
                        .orElse(null))
                .description(decodedInvoice.getDescription())
                .descriptionHash(Optional.ofNullable(decodedInvoice.getDescriptionHash())
                        .map(ByteVector32::toHex)
                        .orElse(null))
                .timestamp(decodedInvoice.getTimestampSeconds())
                .expirySeconds(decodedInvoice.getExpirySeconds())
                .features(decodedInvoice.getFeatures().toHex())
                .minFinalCltvExpiry(Optional.ofNullable(decodedInvoice.getMinFinalExpiryDelta())
                        .map(CltvExpiryDelta::toLong)
                        .orElse(null))
                .fallbackAddress(decodedInvoice.getFallbackAddress())
                .signature(decodedInvoice.getSignature().toHex())
                .routingInfos(decodedInvoice.getRoutingInfo().stream()
                        .map(it -> DecodeInvoiceResponseDto.RoutingInfoDto.builder()
                                .routingHints(it.getHints()
                                        .stream()
                                        .map(hint -> DecodeInvoiceResponseDto.RoutingHintDto.builder()
                                                .pubkey(hint.getNodeId().toHex())
                                                .shortChannelId(hint.getShortChannelId().toString())
                                                .feeBaseMsat(hint.getFeeBase().getMsat())
                                                .feeProportionalMillionths(hint.getFeeProportionalMillionths())
                                                .cltvExpiryDelta(hint.getCltvExpiryDelta().toLong())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build());
    }

}