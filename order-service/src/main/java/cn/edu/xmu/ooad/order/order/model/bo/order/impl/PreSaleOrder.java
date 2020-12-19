package cn.edu.xmu.ooad.order.order.model.bo.order.impl;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.order.centre.utils.SpringUtils;
import cn.edu.xmu.ooad.order.order.dao.PaymentDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.RefundStatus;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.order.model.po.RefundPo;
import cn.edu.xmu.ooad.order.require.IPreSaleService;
import cn.edu.xmu.ooad.order.require.models.PreSaleActivityInfo;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
     * <p>
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
        List<PaymentPo> poList = paymentDao.getPaymentByOrderId(this.getId());
        if (poList == null) {
            // 数据库错误
            return -1L;
        }
        long paidPrice = poList
                .stream()
                .mapToLong(PaymentPo::getAmount)
                .sum();

        // 获取预售活动信息
        PreSaleActivityInfo psai = iPreSaleService.getPreSaleActivity(this.getPresaleId());
        if (psai == null) {
            return -1L;
        }
        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
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
        // 只有「未发货」或「未支付」才能让客户修改
        OrderStatus status = this.getState();
        OrderChildStatus subState = this.getSubstate();
        if (status == OrderStatus.PENDING_PAY) {
            return true;
        } else if (status == OrderStatus.PENDING_RECEIVE) {
            return subState != OrderChildStatus.SHIPPED;
        } else {
            return false;
        }
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
        // 只有未发货的才能被客户取消
        OrderStatus status = this.getState();
        OrderChildStatus subState = this.getSubstate();
        if (status == OrderStatus.PENDING_PAY) {
            return true;
        } else if (status == OrderStatus.PENDING_RECEIVE) {
            return subState != OrderChildStatus.SHIPPED;
        } else {
            return false;
        }
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

    @Override
    public int triggerCancelled() {
        // 根据订单的付款单创建退款单
        PaymentDao paymentDao = SpringUtils.getBean(PaymentDao.class);
        // 获取该订单上的所有支付单
        List<PaymentPo> poList = paymentDao.getPaymentByOrderId(this.getId());
        if (poList == null) {
            // 数据库错误
            return -1;
        }
        // 获取预售活动的预付款金额，预付款不退 TODO  -是这么获取service的吗？
        IPreSaleService iPreSaleService = SpringUtils.getBean(IPreSaleService.class);
//        PreSaleActivityInfo psai;
//        try {
//            psai = iPreSaleService.getPreSaleActivity(this.getPresaleId());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 1;
//        }
//        if (psai == null) {
//            // 失败
//            System.err.println("取消预售订单时，获取预售资讯失败！orderId=" + this.getId());
//            return 1;
//        }
//        // 获取预售价格
//        Long prePaidPrice = psai.getAdvancePayPrice();
        Long prePaidPrice = 0L;
        // 依次创建退款单
        for (PaymentPo paymentPo : poList) {
            // 是不是预付款？
            if (paymentPo.getAmount().equals(prePaidPrice)) {
                continue; // 预付款不退
            }
            // 获取售后单ID
            Long aftersaleId = paymentPo.getAftersaleId();
            // 新建 refund PO对象
            RefundPo refundPo = new RefundPo();
            refundPo.setPaymentId(paymentPo.getId());
            refundPo.setOrderId(this.getId()); // 此订单的 id
            refundPo.setAftersaleId(aftersaleId);
            // 模拟支付环境的都是已经退款
            refundPo.setState(RefundStatus.ALREADY_REFUND.getCode());
            refundPo.setAmount(paymentPo.getAmount());
            refundPo.setGmtCreate(Accessories.secondTime(LocalDateTime.now()));
            refundPo.setGmtModified(Accessories.secondTime(LocalDateTime.now()));
            // TODO - 如果是返点支付，应该把返点回充至账户中
            // 将退款单Po对象插入数据库
            try {
                int response = paymentDao.addRefund(refundPo);
                if (response <= 0) {
                    System.err.println("自动下退款单失败！orderId=" + this.getId());
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return 1;
            }
        }
        return 0;
    }
}
