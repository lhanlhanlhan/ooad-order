package cn.edu.xmu.ooad.order.model.bo.discount.impl.discount;

import cn.edu.xmu.ooad.order.bo.OrderItem;
import cn.edu.xmu.ooad.order.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.discount.BaseCouponLimitation;

import java.util.List;

/**
 * 总价百分比优惠 (优惠金额平摊)
 */
public class PercentageCouponDiscount extends BaseCouponDiscount {

	public PercentageCouponDiscount(){}

	public PercentageCouponDiscount(BaseCouponLimitation limitation, long value) {
		super(limitation, value);
	}

	@Override
	public void calcAndSetDiscount(List<OrderItem> orderItems) {
		for (OrderItem oi : orderItems) {
			oi.setDiscount(value / 100 * oi.getPrice());
		}
	}
}