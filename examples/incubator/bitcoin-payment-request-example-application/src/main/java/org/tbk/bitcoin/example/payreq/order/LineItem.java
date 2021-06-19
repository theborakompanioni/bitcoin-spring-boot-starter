package org.tbk.bitcoin.example.payreq.order;

import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import javax.persistence.Column;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Table(name = "customer_order_line_item")
@AllArgsConstructor
public class LineItem implements Entity<Order, LineItem.LineItemId> {

    private final LineItemId id;

    private final String name;

    private final NumberValue price;

    private final CurrencyUnit currencyUnit;

    private final String displayPrice;

    private int quantity;

    @Column(name = "position")
    private Integer position;

    public LineItem(String name, MonetaryAmount price) {
        this.id = LineItemId.of(UUID.randomUUID());
        this.name = name;
        this.price = price.getNumber();
        this.currencyUnit = price.getCurrency();
        this.displayPrice = price.with(Monetary.getRounding(price.getCurrency())).toString();
        this.quantity = 1;
    }

    LineItem increaseQuantity() {
        this.quantity++;
        return this;
    }

    public MonetaryAmount getPrice() {
        return Money.of(price, currencyUnit).multiply(quantity);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("displayPrice", displayPrice)
                .add("quantity", quantity)
                .add("position", position)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class LineItemId implements Identifier {
        @NonNull
        UUID id;
    }
}