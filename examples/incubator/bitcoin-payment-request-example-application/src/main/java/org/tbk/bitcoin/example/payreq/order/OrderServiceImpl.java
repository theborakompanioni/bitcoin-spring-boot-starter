package org.tbk.bitcoin.example.payreq.order;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @NonNull
    private final Orders orders;

    @Override
    public Order createOrder(Collection<LineItem> lineItems) {
        return orders.save(new Order(lineItems));
    }
}