package org.tbk.bitcoin.example.payreq.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.UUID;


@Getter
@Table(name = "customer_order_line_item")
@AllArgsConstructor
public class LineItem implements Entity<Order, LineItem.LineItemIdentifier> {

    private final LineItemIdentifier id;
    private final String name;
    // private final MonetaryAmount price;
    private final long price;
    //private final Association<Drink, DrinkIdentifier> drink;
    private int quantity;

    @Column(name = "position")
    private Integer position;

    public LineItem(String name, long price) {
        this.id = LineItemIdentifier.of(UUID.randomUUID());
        this.name = name;
        this.quantity = 1;
        this.price = price;
        // this.drink = Association.forAggregate(drink);
    }

    LineItem increaseAmount() {
        this.quantity++;
        return this;
    }

    @Value(staticConstructor = "of")
    public static class LineItemIdentifier implements Identifier {
        UUID id;
    }
}