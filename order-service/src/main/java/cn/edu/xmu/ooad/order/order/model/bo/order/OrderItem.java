package cn.edu.xmu.ooad.order.order.model.bo.order;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderItemVo;
import lombok.Data;

import java.time.LocalDateTime;

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

    private Long shopId;


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

    // 优惠金额是累加吧？亲
    public void addDiscount(Long delta) {
        this.discount += delta;
    }

    // 为了写入数据库，我拼了
    public OrderItemPo toPo() {
        // 创建新 po，设置除了 orderId、beSharedId 以外的资料
        OrderItemPo orderItemPo = new OrderItemPo();
        orderItemPo.setGoodsSkuId(skuId);
        orderItemPo.setQuantity(quantity);
        // 填写各种价格
        orderItemPo.setPrice(price);
        orderItemPo.setDiscount(discount);
        orderItemPo.setName(name);
        orderItemPo.setGmtCreate(Accessories.secondTime(LocalDateTime.now()));
        orderItemPo.setGmtModified(Accessories.secondTime(LocalDateTime.now()));
        orderItemPo.setCouponActivityId(couponActId);
        orderItemPo.setBeShareId(beSharedId);

        return orderItemPo;
    }

    // 为了计算运费，我拼了
    public FreightCalcItem toCalcItem() {
        FreightCalcItem item = new FreightCalcItem();
        item.setCount(this.getQuantity());
        item.setSkuId(this.getSkuId());
        return item;
    }
}
