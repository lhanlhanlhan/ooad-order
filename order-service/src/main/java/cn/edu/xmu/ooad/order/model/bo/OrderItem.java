package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.po.OrderItemPo;

/**
 * 订单项目业务对象
 *
 * @author Han Li
 * Created at 26/11/2020 3:15 下午
 * Modified by Han Li at 6/12/2020 3:15 下午
 */
public class OrderItem {

    private OrderItemPo orderItemPo = null;

    public OrderItem(OrderItemPo po) {
        this.orderItemPo = po;
    }

    public Long getId() {
        return orderItemPo.getId();
    }

    public Long getSkuId() {
        return orderItemPo.getGoodsSkuId();
    }

    public Long getOrderId() {
        return orderItemPo.getOrderId();
    }

    public String getName() {
        return orderItemPo.getName();
    }

    public Integer getQuantity() {
        return orderItemPo.getQuantity();
    }

    public Long getPrice() {
        return orderItemPo.getPrice();
    }

    public Long getDiscount() {
        return orderItemPo.getDiscount();
    }

    public Long getCouponActId() {
        return orderItemPo.getCouponActivityId();
    }

    public Long getBeSharedId() {
        return orderItemPo.getBeShareId();
    }
}
