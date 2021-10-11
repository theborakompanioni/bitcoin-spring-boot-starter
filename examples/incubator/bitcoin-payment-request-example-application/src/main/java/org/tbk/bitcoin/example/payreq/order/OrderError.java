package org.tbk.bitcoin.example.payreq.order;

import lombok.NonNull;
import lombok.Value;


@Value(staticConstructor = "of")
public class OrderError implements OrderStateChanged {

    @NonNull
    Order.OrderId orderId;

    public String toString() {
        return "OrderErrorEvent";
    }
}