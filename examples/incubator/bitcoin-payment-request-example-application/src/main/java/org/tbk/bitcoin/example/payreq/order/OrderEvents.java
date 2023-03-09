package org.tbk.bitcoin.example.payreq.order;

public final class OrderEvents {
    private OrderEvents() {
        throw new UnsupportedOperationException();
    }

    public record CreatedEvent (Order.OrderId orderId) {}
    public record ReadyEvent (Order.OrderId orderId) {}
    public record InProgressEvent (Order.OrderId orderId) {}
    public record CancelledEvent (Order.OrderId orderId) {}
    public record CompletedEvent (Order.OrderId orderId) {}
    public record ErrorEvent (Order.OrderId orderId) {}
}
