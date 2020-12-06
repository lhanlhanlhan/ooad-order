package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.enums.OrderStatus;
import cn.edu.xmu.oomall.order.enums.OrderType;
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
 * 订单业务对象
 *
 * @author Han Li
 * Created at 25/11/2020 4:44 下午
 * Modified by Han Li at 25/11/2020 4:44 下午
 */
@Data
public class Order implements VoCreatable, SimpleVoCreatable {

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

        // 把 orderItemList 中的每个 Item 转换成 bo 对象 (如有)
        if (orderPo.getOrderItemList() != null) {
            this.orderItemList =
                    orderPo.getOrderItemList()
                            .stream()
                            .map(OrderItem::new)
                            .collect(Collectors.toList());
        }
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
     * TODO - 判断该订单可否被支付
     */
    public boolean canPay() {
        return true;
    }

    /**
     * 判断订单是否可被修改
     */
    public boolean canModify() {
        // 订单状态为空，不给修改
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给修改
        if (status == null) {
            return false;
        }
        // 只有「未发货」才能让客户修改
        return status == OrderStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    public boolean canDelete() {
        // 订单状态为空，不给删除
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给删除
        if (status == null) {
            return false;
        }
        // 只有已签收 or 已取消 or 已退款 or 订单终止 or 预售终止的才让删除
        switch (status) {
            case SIGNED:
            case REFUNDED:
            case TERMINATED:
            case PRE_SALE_TERMINATED:
            case CANCELLED:
                return true;
            default:
                return false;
        }
    }

    /**
     * 判断该 订单 是否可被客户取消
     */
    public boolean canCustomerCancel() {
        // 订单状态为空，不给取消
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给不给取消
        if (status == null) {
            return false;
        }

        // 只有未支付的才能被客户取消
        return status == OrderStatus.PENDING_PAY;
        /*
        switch (status) {
            case PENDING_DEPOSIT:
            case PENDING_PAY:
            case PENDING_GROUP:
            case DEPOSIT_PAID:
            case PENDING_REM_BALANCE:
                return true;
            default:
                return false;
        }
         */
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    public boolean canShopCancel() {
        // 订单状态为空，不给取消
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给取消
        if (status == null) {
            return false;
        }
        // 只有未支付的才能被商户取消
        return status == OrderStatus.PENDING_PAY;
        /*
        switch (status) {
            case PENDING_DEPOSIT:
            case PENDING_PAY:
            case PENDING_GROUP:
                return true;
            default:
                return false;
        }
        */
    }

    /**
     * 判断该 订单 是否可被签收
     */
    public boolean canSign() {
        // 订单状态为空，不给签收
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给签收
        if (status == null) {
            return false;
        }
        // 只有订单状态为「已到货」的可以签收
        return status == OrderStatus.REACHED;
    }

    /**
     * 判断该 订单 是否可被发货
     */
    public boolean canDeliver() {
        // 订单状态为空，不给发货
        if (this.getState() == null) {
            return false;
        }
        OrderStatus status = OrderStatus.getByCode(this.getState());
        // 订单状态非法，不给发货
        if (status == null) {
            return false;
        }
        // 只有已支付的普通/预售订单，以及「已成团」的团购订单，才能被发货
        if (status == OrderStatus.PAID) {
            OrderType type = getOrderType();
            switch (type) {
                case NORMAL:
                case PRE_SALE:
                    return true;
                default:
                    // 团购订单，还需要已成团
                    return getSubstate() == OrderStatus.GROUPED.getCode();
            }
        } else {
            return false;
        }
        /*
        switch (status) {
            case REM_BALANCE_PAID:
            case PAID:
            case GROUP_FORMED:
                return true;
            default:
                return false;
        }
        */
    }

    /**
     * 判断该 订单 是否可被从团购转为普通订单
     */
    public boolean canCustomerChangeFromGrouponToNormal() {
        // 订单状态为空，不给转换
        if (this.getState() == null) {
            return false;
        }
        OrderStatus subState = OrderStatus.getByCode(this.getSubstate());
        // 订单状态非法，不给转换
        if (subState == null) {
            return false;
        }
        OrderType type = this.getOrderType();
        // 订单类型为空，不给转换
        if (type == null) {
            return false;
        }

        // 只有订单类型为团购、订单状态为「未到达门槛」的可以改成普通订单
        return type == OrderType.GROUPON && subState == OrderStatus.GROUP_FAILED;
    }


    /**
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
        return orderPo == null ? orderSimplePo.getGmtCreate() : orderPo.getGmtCreate();
    }

    public LocalDateTime getGmtModified() {
        return orderPo == null ? null : orderPo.getGmtModified();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

}
