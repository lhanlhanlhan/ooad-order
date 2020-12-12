package cn.edu.xmu.ooad.order.model.bo.discount.impl.discount;

import cn.edu.xmu.ooad.order.bo.OrderItem;
import cn.edu.xmu.ooad.order.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.discount.BaseCouponLimitation;

import java.util.List;

/**
 * 按最便宜商品的百分比优惠 (优惠金额平摊)
 */
public class CheapestPercentageDiscount extends BaseCouponDiscount {

	public CheapestPercentageDiscount() {}

	public CheapestPercentageDiscount(BaseCouponLimitation limitation, long value) {
		super(limitation, value);
	}

	@Override
	public void calcAndSetDiscount(List<OrderItem> orderItems) {
		int min = Integer.MAX_VALUE;
		int total = 0;
		for (int i = 0; i < orderItems.size(); i++) {
			OrderItem oi = orderItems.get(i);
			total += oi.getPrice() * oi.getQuantity();
			if (oi.getPrice() < min) {
				min = i;
			}
		}

		long discount = (long) ((1.0 * value / 100) * orderItems.get(min).getPrice());

		for (OrderItem oi : orderItems) {
			oi.setDiscount((long) ((1.0 * oi.getPrice() * oi.getQuantity()) / total * discount / oi.getQuantity()));
		}
	}
}
