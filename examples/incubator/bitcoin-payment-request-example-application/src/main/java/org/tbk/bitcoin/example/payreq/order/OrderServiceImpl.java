package org.tbk.bitcoin.example.payreq.order;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequest;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequestService;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequestStateChanged;

import java.util.Collection;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @NonNull
    private final Orders orders;

    @NonNull
    private final PaymentRequestService paymentRequestService;

    @Override
    public Order createOrder(Collection<LineItem> lineItems) {
        Order order = new Order(lineItems);
        return orders.save(order);
    }

    @Async
    @Transactional
    @TransactionalEventListener
    void on(OrderCreated event) {
        Order order = orders.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Could not find Order from OrderCreated event"));

        orders.save(order.markReady());
    }

    @Async
    @Transactional
    @TransactionalEventListener
    void on(OrderReady event) {
        Order order = orders.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Could not find Order from OrderReady event"));

        this.reevaluateOrder(order);
    }

    @Async
    @Transactional
    @TransactionalEventListener
    void on(PaymentRequestStateChanged event) {
        Order order = paymentRequestService.findPaymentRequestBy(event.getPaymentRequestId())
                .flatMap(it -> orders.findById(it.getOrder().getId()))
                .orElseThrow(() -> new IllegalStateException("Could not find Order from PaymentRequestStateChanged event"));

        this.reevaluateOrder(order);
    }

    private Order reevaluateOrder(Order order) {
        if (!order.canReevaluate()) {
            throw new IllegalStateException("Order cannot be reevaluated.");
        }

        if (!order.isInProgress()) {
            order.markInProgress();
        }

        PaymentRequest paymentRequest = paymentRequestService.findPaymentRequestFor(order)
                .orElseThrow(() -> new IllegalStateException("Could not find PaymentRequest for Order"));

        switch (paymentRequest.getStatus()) {
            case COMPLETED:
                order = order.markCompleted();
                break;
            case CANCELLED:
            case EXPIRED:
                order = order.markError();
                break;
        }

        log.debug("Reevaluating order {} finished in state {}.", order.getId(), order.getStatus());

        return orders.save(order);
    }
}