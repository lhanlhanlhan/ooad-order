package cn.edu.xmu.ooad.order.order.model.bo.discount.impl.limitation;

import cn.edu.xmu.ooad.order.order.model.bo.discount.BaseCouponLimitation;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;

import java.util.List;

/**
 * 满 x 元减门槛金额
 */
public class PriceCouponLimitation extends BaseCouponLimitation {

    public PriceCouponLimitation() {

    }

    public PriceCouponLimitation(long value) {
        super(value);
    }

    @Override
    public boolean pass(List<OrderItem> orderItems) {
        long t = 0;
        for (OrderItem oi : orderItems) {
            t += oi.getQuantity() * oi.getPrice();
        }
        return t > value;
    }

}
