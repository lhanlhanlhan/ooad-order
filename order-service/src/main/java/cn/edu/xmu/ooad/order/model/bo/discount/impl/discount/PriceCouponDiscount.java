package cn.edu.xmu.ooad.order.model.bo.discount.impl.discount;

import cn.edu.xmu.ooad.order.bo.OrderItem;
import cn.edu.xmu.ooad.order.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.discount.BaseCouponLimitation;

import java.util.List;

/**
 * 立减金额百分比优惠 (优惠金额平摊)
 */
public class PriceCouponDiscount extends BaseCouponDiscount {

	public PriceCouponDiscount(){}

	public PriceCouponDiscount(BaseCouponLimitation limitation, long value) {
		super(limitation, value);
	}

	@Override
	public void calcAndSetDiscount(List<OrderItem> orderItems) {
		long total = 0L;
		for (OrderItem oi : orderItems) {
			total += oi.getPrice() * oi.getQuantity();
		}

		for (OrderItem oi : orderItems) {
			long discount = (long) ((1.0 * oi.getQuantity() * oi.getPrice() / total) * value / oi.getQuantity());
			oi.setDiscount(discount);
		}
	}
}
