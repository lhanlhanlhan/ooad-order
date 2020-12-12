package cn.edu.xmu.ooad.order.model.bo.discount;

import cn.edu.xmu.ooad.order.model.bo.OrderItem;

import java.util.List;

/**
 * 标明可计算式优惠：
 */
public interface Computable {

	List<OrderItem> compute(List<OrderItem> orderItems);
}
