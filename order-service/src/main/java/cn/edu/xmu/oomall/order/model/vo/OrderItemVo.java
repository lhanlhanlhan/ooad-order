package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import lombok.Data;

/**
 * OrderItem 的 Vo 对象
 *
 * @author Han Li
 * Created at 26/11/2020 3:59 下午
 * Modified by Han Li at 26/11/2020 3:59 下午
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private Long orderId;
    private String name;
    private Integer quantity;
    private Long price;
    private Long discount;
    private Long couponActId;
    private Long beSharedId;

    /**
     * 用一个 Bo 对象初构造本对象
     * @param item Bo 对象
     */
    public OrderItemVo(OrderItem item) {
        this.skuId = item.getSkuId();
        this.orderId = item.getOrderId();
        this.name = item.getName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.discount = item.getDiscount();
        this.couponActId = item.getCouponActId();
        this.beSharedId = item.getBeSharedId();
    }
}
