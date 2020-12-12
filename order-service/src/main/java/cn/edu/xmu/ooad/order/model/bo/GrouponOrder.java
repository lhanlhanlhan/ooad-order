package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.po.OrderSimplePo;

import java.util.List;

/**
 * @author Han Li
 * Created at 11/12/2020 10:16 上午
 * Modified by Han Li at 11/12/2020 10:16 上午
 */
public class GrouponOrder extends Order {

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public GrouponOrder(OrderSimplePo orderSimplePo) {
        super(orderSimplePo);
    }

    /**
     * 创建完整业务对象
     *
     * @param orderPo Po 对象
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     */
    public GrouponOrder(OrderPo orderPo) {
        super(orderPo);
    }

    /**
     * 支付成功后调用分单，分成若干个完整订单【每个订单内含 orderItemList，可以直接插入】
     *
     * @return 如果分单成功，返回 List；如果无需分单，返回 null
     */
    @Override
    public List<Order> splitToOrders() {
        return null;
    }

    /**
     * 判断该订单可否被支付
     */
    @Override
    public boolean canPay() {
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断订单是否可被修改
     */
    @Override
    public boolean canModify() {
        // 只有「未发货」才能让客户修改
        return this.getState() == OrderStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    @Override
    public boolean canDelete() {
        OrderStatus status = this.getState();
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
    @Override
    public boolean canCustomerCancel() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给不给取消
        if (status == null) {
            return false;
        }

        // 只有未支付的才能被客户取消
        return status == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    @Override
    public boolean canShopCancel() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给取消
        if (status == null) {
            return false;
        }
        // 只有未支付的才能被商户取消
        return status == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被签收
     */
    @Override
    public boolean canSign() {
        OrderStatus status = this.getState();
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
    @Override
    public boolean canDeliver() {
        OrderStatus status = this.getState();
        // 订单状态非法，不给发货
        if (status == null) {
            return false;
        }
        // 只有「已成团」的团购订单，才能被发货
        return getSubstate() == OrderStatus.GROUPED;
    }

    /**
     * 判断该 团购订单 是否可被从团购转为普通订单
     */
    public boolean canChangeToNormal() {
        OrderStatus subState = this.getSubstate();
        // 订单状态非法，不给转换
        if (subState == null) {
            return false;
        }

        // 只有订单类型为团购、订单状态为「未到达门槛」的可以改成普通订单
        return subState == OrderStatus.GROUP_FAILED;
    }
}
