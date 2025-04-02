package org.tbk.lightning.client.common.core;

import org.tbk.lightning.client.common.core.proto.*;
import reactor.core.publisher.Mono;

public interface LightningCommonClient<T> {
    Mono<CommonInfoResponse> info(CommonInfoRequest request);

    Mono<CommonConnectResponse> connect(CommonConnectRequest request);

    Mono<CommonNewAddressResponse> newAddress(CommonNewAddressRequest request);

    Mono<CommonCreateInvoiceResponse> createInvoice(CommonCreateInvoiceRequest request);

    Mono<CommonListPeersResponse> listPeers(CommonListPeersRequest request);

    Mono<CommonOpenChannelResponse> openChannel(CommonOpenChannelRequest request);

    Mono<CommonListUnspentResponse> listUnspent(CommonListUnspentRequest request);

    Mono<CommonListPeerChannelsResponse> listPeerChannels(CommonListPeerChannelsRequest request);

    Mono<CommonPayResponse> pay(CommonPayRequest request);

    Mono<CommonLookupInvoiceResponse> lookupInvoice(CommonLookupInvoiceRequest request);

    Mono<CommonLookupPaymentResponse> lookupPayment(CommonLookupPaymentRequest request);

    Mono<CommonQueryRouteResponse> queryRoutes(CommonQueryRouteRequest request);

    // TODO: consider removing access to the underlying client after all common functions are implemented
    T baseClient();
}
