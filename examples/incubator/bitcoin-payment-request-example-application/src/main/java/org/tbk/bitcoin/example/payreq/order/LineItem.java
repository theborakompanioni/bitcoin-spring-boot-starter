package org.tbk.bitcoin.example.payreq.order;

import com.google.common.base.MoreObjects;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.tbk.bitcoin.example.payreq.common.MonetaryAmountFormats;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.util.UUID;

@Getter
@Table(name = "customer_order_line_item")
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
        this.id = LineItemId.create();
        this.name = name;
        this.price = price.getNumber();
        this.currencyUnit = price.getCurrency();
        this.displayPrice = MonetaryAmountFormats.bitcoin.format(price);
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

        public static LineItem.LineItemId create() {
            return LineItem.LineItemId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }
}