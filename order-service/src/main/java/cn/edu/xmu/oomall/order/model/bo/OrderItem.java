package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.interfaces.AuthenticObject;
import cn.edu.xmu.oomall.order.interfaces.VoCreatable;
import cn.edu.xmu.oomall.order.model.po.OrderItemPo;
import cn.edu.xmu.oomall.order.model.vo.OrderItemVo;

/**
 * @author Han Li
 * Created at 26/11/2020 3:15 下午
 * Modified by Han Li at 26/11/2020 3:15 下午
 */
public class OrderItem implements VoCreatable, AuthenticObject {

    private OrderItemPo orderItemPo = null;
    public OrderItem(OrderItemPo po) {
        this.orderItemPo = po;
    }

    /**
     * 判断该对象是否被篡改
     *
     * @return 是否被篡改，若被篡改，返回 false
     */
    @Override
    public boolean isAuthentic() {
        return true;
    }

    /**
     * 获取对象的签名
     *
     * @return 对象的签名
     */
    @Override
    public String calcSignature() {
        return "";
    }

    /**
     * 创建 Vo 对象
     *
     * @return Vo 对象
     */
    @Override
    public OrderItemVo createVo() {
        return new OrderItemVo(this);
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
