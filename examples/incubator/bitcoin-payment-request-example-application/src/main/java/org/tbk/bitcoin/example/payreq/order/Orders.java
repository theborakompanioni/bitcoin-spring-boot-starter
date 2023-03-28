package org.tbk.bitcoin.example.payreq.order;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Orders extends CrudRepository<Order, Order.OrderId>,
        PagingAndSortingRepository<Order, Order.OrderId>,
        AssociationResolver<Order, Order.OrderId> {

    /**
     * Returns all {@link Order}s with the given {@link Order.Status}.
     *
     * @param status must not be {@literal null}.
     * @return all orders in given status
     */
    List<Order> findByStatus(@Param("status") Order.Status status);

}