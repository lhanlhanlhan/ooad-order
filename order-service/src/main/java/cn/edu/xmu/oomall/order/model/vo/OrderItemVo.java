package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.bo.OrderItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("skuId", this.skuId);
        map.put("orderId", this.orderId);
        map.put("name", this.name);
        map.put("quantity", this.quantity);
        map.put("price", this.price);
        map.put("discount", this.discount);
        map.put("couponActId", this.couponActId);
        map.put("beSharedId", this.beSharedId);
        return map;
    }
}
