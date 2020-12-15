package cn.edu.xmu.ooad.order.order.model.bo.discount.impl.limitation;

import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.bo.discount.BaseCouponLimitation;

import java.util.List;

/**
 * 满 x 件减门槛
 */
public class AmountCouponLimitation extends BaseCouponLimitation {

    public AmountCouponLimitation() {
    }

    public AmountCouponLimitation(long value) {
        super(value);
    }

    @Override
    public boolean pass(List<OrderItem> orderItems) {
        long t = 0;
        for (OrderItem oi : orderItems) {
            t += oi.getQuantity();
        }
        return t >= value;
    }
}
