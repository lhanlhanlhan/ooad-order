package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.model.vo.OrderItemVo;
import lombok.Data;

/**
 * 订单项目业务对象
 *
 * @author Han Li
 * Created at 26/11/2020 3:15 下午
 * Modified by Han Li at 6/12/2020 3:15 下午
 */
@Data
public class OrderItem {

    private Long id;
    private Long skuId;
    private Long orderId;
    private String name;
    private Integer quantity;
    private Long price;
    private Long discount;
    private Long couponActId;
    private Long beSharedId;


    // 为了计算优惠，我拼了
    public OrderItem(OrderItemVo vo) {
        this.skuId = vo.getSkuId();
        this.orderId = vo.getOrderId();
        this.name = vo.getName();
        this.quantity = vo.getQuantity();
        this.price = vo.getPrice();
        this.discount = vo.getDiscount();
        this.couponActId = vo.getCouponActId();
        this.beSharedId = vo.getBeSharedId();
    }

    public OrderItem(OrderItemPo vo) {
        this.id = vo.getId();
        this.skuId = vo.getGoodsSkuId();
        this.orderId = vo.getOrderId();
        this.name = vo.getName();
        this.quantity = vo.getQuantity();
        this.price = vo.getPrice();
        this.discount = vo.getDiscount();
        this.couponActId = vo.getCouponActivityId();
        this.beSharedId = vo.getBeShareId();
    }
}
