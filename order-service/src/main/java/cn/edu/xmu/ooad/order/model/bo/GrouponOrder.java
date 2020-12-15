package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.dao.PaymentDao;
import cn.edu.xmu.ooad.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.SpringUtils;

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
     * 判断该订单要支付的金额
     *
     * 返回：-1：内部错误；>= 0：应付金额
     */
    @Override
    public long shallPayPrice() {
        if (this.getSubstate() != OrderChildStatus.NEW) {
            return 0L;
        }
        // 获取已支付之金额
        PaymentDao paymentDao = SpringUtils.getBean(PaymentDao.class);
        // 获取该订单上的所有支付单
        APIReturnObject<List<PaymentPo>> poListObj = paymentDao.getPaymentOrderByOrderId(this.getId());
        if (poListObj.getCode() != ResponseCode.OK) {
            // 数据库错误
            return -1L;
        }
        List<PaymentPo> poList = poListObj.getData();
        // 获取已支付之金额
        long paidPrice = poList
                .stream()
                .mapToLong(PaymentPo::getAmount)
                .sum();
        // 获得总共需付款 (总共需付款 = 訂單總價+運費-優惠)
        long allShallPayPrice = getOriginPrice() +
                getFreightPrice() -
                getDiscountPrice();

        return allShallPayPrice - paidPrice;
    }

    /**
     * 判断订单是否可被修改
     */
    @Override
    public boolean canModify() {
        // 只有「未发货」才能让客户修改
        return this.getSubstate() == OrderChildStatus.PAID;
    }

    /**
     * 判断该订单是否可被删除
     */
    @Override
    public boolean canDelete() {
        return (this.getState() == OrderStatus.CANCELLED || this.getState() == OrderStatus.DONE);
    }

    /**
     * 判断该 订单 是否可被客户取消
     */
    @Override
    public boolean canCustomerCancel() {
        // 只有未支付的才能被客户取消
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被商户取消
     */
    @Override
    public boolean canShopCancel() {
        // 只有未支付的才能被商户取消
        return this.getState() == OrderStatus.PENDING_PAY;
    }

    /**
     * 判断该 订单 是否可被签收
     */
    @Override
    public boolean canSign() {
        // 只有订单状态为「已发货」的可以签收
        return getSubstate() == OrderChildStatus.SHIPPED;
    }

    /**
     * 判断该 订单 是否可被发货
     */
    @Override
    public boolean canDeliver() {
        // 只有「已付款完成 (已成团)」的团购订单，才能被发货
        return getSubstate() == OrderChildStatus.PAID;
    }

    /**
     * 判断该 团购订单 是否可被从团购转为普通订单
     */
    public boolean canChangeToNormal() {
        // 只有订单类型为团购、订单状态为「未到达门槛」的可以改成普通订单
        return getSubstate() == OrderChildStatus.GROUP_FAILED;
    }

    @Override
    public void triggerPaid() {
        // 订单状态改成已支付、待成团
        this.setState(OrderStatus.PENDING_RECEIVE);
        this.setSubstate(OrderChildStatus.PENDING_GROUP);
    }
}
