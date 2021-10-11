package org.tbk.bitcoin.example.payreq.order;

import lombok.NonNull;
import lombok.Value;


@Value(staticConstructor = "of")
public class OrderInProgress implements OrderStateChanged {

    @NonNull
    Order.OrderId orderId;

    public String toString() {
        return "OrderInProgressEvent";
    }
}