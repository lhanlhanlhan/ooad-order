package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.interfaces.AuthenticObject;
import cn.edu.xmu.oomall.order.interfaces.SimpleVoCreatable;
import cn.edu.xmu.oomall.order.interfaces.VoCreatable;
import cn.edu.xmu.oomall.order.model.po.OrderPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.OrderSimpleVo;
import cn.edu.xmu.oomall.order.model.vo.OrderVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 *
 * @author Han Li
 * Created at 25/11/2020 4:44 下午
 * Modified by Han Li at 25/11/2020 4:44 下午
 */
@Data
public class Order implements VoCreatable, SimpleVoCreatable, AuthenticObject {

    private List<OrderItem> orderItemList;

    // 概要业务对象 【代理】
    private OrderSimplePo orderSimplePo = null;

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public Order(OrderSimplePo orderSimplePo) {
        this.orderSimplePo = orderSimplePo;
    }

    // 完整业务对象 【代理】
    private OrderPo orderPo = null;
    /**
     * 创建完整业务对象
     *
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     * @param orderPo Po 对象
     */
    public Order(OrderPo orderPo) {
        this.orderPo = orderPo;

        // 把 orderItemList 中的每个 Item 转换成 bo 对象
        this.orderItemList =
                orderPo.getOrderItemList()
                .stream()
                .map(OrderItem::new)
                .filter(OrderItem::isAuthentic)
                .collect(Collectors.toList());
    }

    /**
     * 创建概要 Vo 对象
     *
     * @return 概要 Vo 对象
     */
    @Override
    public OrderSimpleVo createSimpleVo() {
        return new OrderSimpleVo(this);
    }

    /**
     * 创建 Vo 对象
     *
     * @return Vo 对象
     */
    @Override
    public OrderVo createVo() {
        return new OrderVo(this);
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


    /*
    Getters
     */

    public Long getId() {
        return orderPo == null ? orderSimplePo.getId() : orderPo.getId();
    }

    public Long getCustomerId() {
        return orderPo == null ? orderSimplePo.getCustomerId() : orderPo.getCustomerId();
    }

    public Long getShopId() {
        return orderPo == null ? orderSimplePo.getShopId() : orderPo.getShopId();
    }

    public String getOrderSn() {
        return orderPo == null ? orderSimplePo.getOrderSn() : orderPo.getOrderSn();
    }

    public Long getPid() {
        return orderPo == null ? orderSimplePo.getPid() : orderPo.getPid();
    }

    public String getConsignee() {
        return orderPo == null ? null : orderPo.getConsignee();
    }

    public Long getRegionId() {
        return orderPo == null ? null : orderPo.getRegionId();
    }

    public String getAddress() {
        return orderPo == null ? null : orderPo.getAddress();
    }

    public String getMobile() {
        return orderPo == null ? null : orderPo.getMobile();
    }

    public String getMessage() {
        return orderPo == null ? null : orderPo.getMessage();
    }

    public Byte getOrderType() {
        return orderPo == null ? orderSimplePo.getOrderType() : orderPo.getOrderType();
    }

    public Long getFreightPrice() {
        return orderPo == null ? orderSimplePo.getFreightPrice() : orderPo.getFreightPrice();
    }

    public Long getCouponId() {
        return orderPo == null ? null : orderPo.getCouponId();
    }

    public Long getCouponActivityId() {
        return orderPo == null ? null : orderPo.getCouponActivityId();
    }

    public Long getDiscountPrice() {
        return orderPo == null ? orderSimplePo.getDiscountPrice() : orderPo.getDiscountPrice();
    }

    public Long getOriginPrice() {
        return orderPo == null ? orderSimplePo.getOriginPrice() : orderPo.getOriginPrice();
    }

    public Long getPresaleId() {
        return orderPo == null ? null : orderPo.getPresaleId();
    }

    public Long getGrouponId() {
        return orderPo == null ? null : orderPo.getGrouponId();
    }

    public Long getGrouponDiscount() {
        return orderPo == null ? null : orderPo.getGrouponDiscount();
    }

    public Integer getRebateNum() {
        return orderPo == null ? null : orderPo.getRebateNum();
    }

    public LocalDateTime getConfirmTime() {
        return orderPo == null ? null : orderPo.getConfirmTime();
    }

    public String getShipmentSn() {
        return orderPo == null ? null : orderPo.getShipmentSn();
    }

    public Byte getState() {
        return orderPo == null ? orderSimplePo.getState() : orderPo.getState();
    }

    public Byte getSubstate() {
        return orderPo == null ? orderSimplePo.getSubstate() : orderPo.getSubstate();
    }

    public Byte getBeDeleted() {
        return orderPo == null ? null : orderPo.getBeDeleted();
    }

    public LocalDateTime getGmtCreated() {
        return orderPo == null ? orderSimplePo.getGmtCreated() : orderPo.getGmtCreated();
    }

    public LocalDateTime getGmtModified() {
        return orderPo == null ? null : orderPo.getGmtModified();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

}
