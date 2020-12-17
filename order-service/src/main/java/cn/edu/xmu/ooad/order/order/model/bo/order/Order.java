package cn.edu.xmu.ooad.order.order.model.bo.order;

import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.GrouponOrder;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.NormalOrder;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.PreSaleOrder;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单业务对象
 *
 * @author Han Li
 * Created at 25/11/2020 4:44 下午
 * Modified by Han Li at 25/11/2020 4:44 下午
 */
@NoArgsConstructor
public abstract class Order {

    protected List<OrderItem> orderItemList;

    // 概要业务对象 【代理】
    private OrderSimplePo orderSimplePo = null;
    // 完整业务对象 【代理】
    private OrderPo orderPo = null;

    /**
     * 创建概要业务对象
     */
    public Order(OrderSimplePo orderSimplePo) {
        this.orderSimplePo = orderSimplePo;
    }

    /**
     * 创建完整业务对象
     *
     * @param orderPo Po 对象
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     */
    public Order(OrderPo orderPo) {
        this.orderPo = orderPo;

        // 把 orderItemList 中的每个 Item 转换成 bo 对象 (如有)
        if (orderPo.getOrderItemList() != null) {
            this.orderItemList =
                    orderPo.getOrderItemList()
                            .stream()
                            .map(OrderItem::new)
                            .collect(Collectors.toList());
        }
    }

    public static Order createSimpleOrder(OrderSimplePo orderSimplePo) {
        switch (orderSimplePo.getOrderType()) {
            case 0: // 普通订单
                return new NormalOrder(orderSimplePo);
            case 1: // 团购
                return new GrouponOrder(orderSimplePo);
            case 2: // 预售
                return new PreSaleOrder(orderSimplePo);
            default:
                return null;
        }
    }

    /**
     * 支付成功后调用分单，分成若干个完整订单【每个订单内含 orderItemList，可以直接插入】
     *
     * @return 如果分单成功，返回 List；如果无需分单，返回 null
     */
    public abstract List<Order> splitToOrders();

    /*
    订单状态的一些判定
     */

    /**
     * 判断该订单目前可被支付之金额
     * <p>
     * 返回：-1：内部错误；>= 0：应付金额
     */
    public abstract long shallPayPrice();

    /**
     * 判断订单是否可被修改
     */
    public abstract boolean canModify();

    /**
     * 判断该订单是否可被删除
     */
    public abstract boolean canDelete();

    /**
     * 判断该 订单 是否可被客户取消
     */
    public abstract boolean canCustomerCancel();

    /**
     * 判断该 订单 是否可被商户取消
     */
    public abstract boolean canShopCancel();

    /**
     * 判断该 订单 是否可被签收
     */
    public abstract boolean canSign();

    /**
     * 判断该 订单 是否可被发货
     */
    public abstract boolean canDeliver();


    /*
     * 订单的一些触发器
     */

    /**
     * 成功创建一笔支付后的触发器
     */
    public abstract void triggerPaid();

    /**
     * 成功发起取消订单后的触发器
     *
     * @return 0：可以取消订单；1：订单不能取消 (发生错误)
     */
    public abstract int triggerCancelled();

    /*
     * Getters
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

    public OrderType getOrderType() {
        return orderPo == null
                ? OrderType.getTypeFromCode(orderSimplePo.getOrderType())
                : OrderType.getTypeFromCode(orderPo.getOrderType());
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

    public OrderStatus getState() {
        Byte state = orderPo == null ? orderSimplePo.getState() : orderPo.getState();
        if (state == null) {
            return null;
        }
        // 订单状态非法，不给修改
        return OrderStatus.getByCode(state);
    }

    public OrderChildStatus getSubstate() {
        Byte state = orderPo == null ? orderSimplePo.getSubstate() : orderPo.getSubstate();
        if (state == null) {
            return null;
        }
        // 订单状态非法，不给修改
        return OrderChildStatus.getByCode(state);
    }

    public Byte getBeDeleted() {
        return orderPo == null ? null : orderPo.getBeDeleted();
    }

    public LocalDateTime getGmtCreated() {
        return orderPo == null ? orderSimplePo.getGmtCreate() : orderPo.getGmtCreate();
    }

    public LocalDateTime getGmtModified() {
        return orderPo == null ? null : orderPo.getGmtModified();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public OrderPo getOrderPo() {
        return orderPo;
    }
}
