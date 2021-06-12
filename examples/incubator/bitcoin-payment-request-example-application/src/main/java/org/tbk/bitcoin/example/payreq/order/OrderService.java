package org.tbk.bitcoin.example.payreq.order;

import java.util.Collection;

public interface OrderService {

    Order createOrder(Collection<LineItem> lineItems);
}