package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.dao.PaymentDao;
import cn.edu.xmu.ooad.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.require.IPreSaleService;
import cn.edu.xmu.ooad.order.require.models.PreSaleActivityInfo;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.order.utils.SpringUtils;
import org.apache.dubbo.config.annotation.DubboReference;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Han Li
 * Created at 11/12/2020 10:16 上午
 * Modified by Han Li at 11/12/2020 10:16 上午
 */
public class PreSaleOrder extends Order {

    /**
     * 创建完整业务对象
     *
     * @param orderPo Po 对象
     * @author Han Li
     * Created at 26/11/2020 15:31
     * Created by Han Li at 26/11/2020 15:31
     */
    public PreSaleOrder(OrderPo orderPo) {
        super(orderPo);
    }

    /**
     * 创建概要业务对象
     *
     * @param orderSimplePo SimplePo 对象
     */
    public PreSaleOrder(OrderSimplePo orderSimplePo) {
        super(orderSimplePo);
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
        if (this.getSubstate() == null) {
            return 0L;
        }

        long shallPayPrice = 0;

        // TODO - 能这样取接口嘛
        IPreSaleService iPreSaleService = SpringUtils.getBean(IPreSaleService.class);

        // 获取已支付之金额
        PaymentDao paymentDao = SpringUtils.getBean(PaymentDao.class);
        // 获取该订单上的所有支付单
        APIReturnObject<List<PaymentPo>> poListObj = paymentDao.getPaymentOrderByOrderId(this.getId());
        if (poListObj.getCode() != ResponseCode.OK) {
            // 数据库错误
            return -1L;
        }
        List<PaymentPo> poList = poListObj.getData();
        long paidPrice = poList
                .stream()
                .mapToLong(PaymentPo::getAmount)
                .sum();

        // 获取预售活动信息
        PreSaleActivityInfo psai = iPreSaleService.getPreSaleActivity(this.getPresaleId());
        if (psai == null) {
            return -1L;
        }
        LocalDateTime nowTime = LocalDateTime.now();
        // 看看现在是属于什么状态
        if (nowTime.isAfter(psai.getStartTime()) && nowTime.isBefore(psai.getPayTime())) {
            // 首款支付时间 (预售没有优惠) 首款-已支付
            shallPayPrice = psai.getAdvancePayPrice() - paidPrice;
            return Math.max(0L, shallPayPrice);
        } else if (nowTime.isAfter(psai.getPayTime()) && nowTime.isBefore(psai.getEndTime())) {
            // 尾款支付时间 (预售没有优惠) (首款+尾款-优惠金额)-已支付+运费
            shallPayPrice = psai.getAdvancePayPrice() +
                    psai.getRestPayPrice() -
                    this.getDiscountPrice() -
                    paidPrice +
                    this.getFreightPrice();
            return Math.max(0L, shallPayPrice);
        } else {
            return 0L;
        }
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

    @Override
    public void triggerPaid() {
        if (this.getSubstate() == OrderChildStatus.PENDING_REM_BALANCE) {
            // 改成付款完成
            this.setState(OrderStatus.PENDING_RECEIVE);
            this.setSubstate(OrderChildStatus.PAID);
        } else {
            // 改成待支付尾款
            this.setState(OrderStatus.PENDING_PAY);
            this.setSubstate(OrderChildStatus.PENDING_REM_BALANCE);
        }
    }
}
