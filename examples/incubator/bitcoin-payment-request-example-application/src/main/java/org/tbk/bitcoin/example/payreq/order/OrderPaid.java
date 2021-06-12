package org.tbk.bitcoin.example.payreq.order;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderPaid {

	private final Order.OrderIdentifier orderId;

	/**
	 * Creates a new {@link OrderPaid}
	 *
	 * @param orderId the id of the order that just has been payed
	 */
	public OrderPaid(Order.OrderIdentifier orderId) {
		this.orderId = orderId;
	}
}