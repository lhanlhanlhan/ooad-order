package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.service.AfterSaleService;
import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.dao.PaymentDao;
import cn.edu.xmu.oomall.order.enums.*;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.OrderEditPo;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import cn.edu.xmu.oomall.order.model.po.RefundPo;
import cn.edu.xmu.oomall.order.model.vo.PaymentInfoVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentOrderVo;
import cn.edu.xmu.oomall.order.model.vo.RefundVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.Accessories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付服务
 *
 * @author 苗新宇
 * Created at 27/11/2020 8:45
 * Modified by  苗新宇 at  27/11/2020 8:45
 */
@Service
public class PaymentService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private AfterSaleService afterSaleService;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private OrderDao orderDao;


    /**
     * 服务 p3：买家为订单创建支付单
     *
     * TODO - 分单！
     * @return APIReturnObject
     * @author 苗新宇
     * Created at 30/11/2020 8:45
     * Modified by Han Li at 5/12/2020 17:08
     */
    @Transactional
    public APIReturnObject<?> createPaymentOrder(Long orderId, Long customerId, PaymentInfoVo paymentInfoVo) {
        // 校验订单 id 是否存在 / 属于用户？
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(orderId, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObject;
        }
        Order simpleOrder = returnObject.getData();

        // 看看现在的状态能不能支付
        if (!simpleOrder.canPay()) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW, "基于订单状态，您不能支付这笔订单！");
        }

        // 获取已支付之金额 TODO 还有没有更优秀的方法啊我操
        // 获取该订单上的所有支付单
        APIReturnObject<List<PaymentPo>> poListObj = paymentDao.getPaymentOrderByOrderId(orderId);
        if (poListObj.getCode() != ResponseCode.OK) {
            // 数据库错误
            return poListObj;
        }
        List<PaymentPo> poList = poListObj.getData();
        // 获取已支付之金额
        long paidPrice = poList
                .stream()
                .mapToLong(PaymentPo::getAmount)
                .sum();

        // 看看有没有超额支付，要超额支付的话，不让支付
        long shallPayPrice = simpleOrder.getOriginPrice() - simpleOrder.getDiscountPrice(); // 总共需付款
        if (paidPrice + paymentInfoVo.getPrice() > shallPayPrice) {
            // 企图超额支付
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.PAY_MORE);
        }

        // 创建支付单Po对象:
        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setOrderId(orderId);

        // 钱
        paymentPo.setActualAmount(paymentInfoVo.getPrice()); // TODO - 你前端只传了1个参数，让我咋区分ActualPrice和Price？？
        paymentPo.setAmount(paymentInfoVo.getPrice());

        // 设各种时间
        LocalDateTime nowTime = LocalDateTime.now();
        paymentPo.setPayTime(nowTime);
        paymentPo.setBeginTime(nowTime);
        paymentPo.setEndTime(nowTime);
        paymentPo.setGmtCreate(nowTime);
        paymentPo.setGmtModified(null);

        // 模拟支付，状态设置为「已支付」，方式为 "002"
        paymentPo.setState(PaymentStatus.PAID.getCode());
        paymentPo.setPaymentPattern(PayPattern.MOCK.getCode());

        // 其他资讯
        paymentPo.setAftersaleId(null); // 将 aftersaleId 设置为空
        paymentPo.setPaySn(Accessories.genSerialNumber()); // 支付流水号

        // 将支付单Po对象插入数据库
        try {
            int response = paymentDao.addPaymentOrder(paymentPo);
            if (response <= 0) {
                logger.error("新支付单插入错误, orderId=" + orderId);
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 判断是否足额支付
        paidPrice += paymentPo.getAmount();
        // 已足额支付，更改订单状态
        if (paidPrice == shallPayPrice) {
            OrderEditPo editPo = new OrderEditPo();
            editPo.setId(orderId);
            switch (simpleOrder.getOrderType()) {
                case GROUPON:   // 团购订单，改为已参团
                    editPo.setState(OrderStatus.PAID.getCode());
                    editPo.setSubState(OrderStatus.GROUP_FORMED.getCode());
                    break;
                case PRE_SALE:  // 预售订单，改为已支付定金 | 已支付尾款
                    // 如果是已支付定金
                    Byte subState = simpleOrder.getSubstate();
                    if (subState.equals(OrderStatus.DEPOSIT_PAID.getCode())) {
                        // 改为已支付 + 已支付尾款
                        editPo.setState(OrderStatus.PAID.getCode());
                        editPo.setSubState(OrderStatus.REM_BALANCE_PAID.getCode());
                    }
                    // 未支付定金
                    else {
                        // 未支付 + 已支付定金 (不是待支付尾款，因为尾款支付时间还没到)
                        editPo.setState(OrderStatus.PENDING_PAY.getCode());
                        editPo.setSubState(OrderStatus.DEPOSIT_PAID.getCode());
                    }
                    break;
                default:
                    // 已支付
                    editPo.setState(OrderStatus.PAID.getCode());
                    break;
            }
            APIReturnObject<?> modifyRet = orderDao.modifyOrder(editPo);
            if (modifyRet.getCode() != ResponseCode.OK) {
                // 改变状态失败，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return modifyRet;
            }
        } else if (paidPrice > shallPayPrice) {
            // 不小心超额支付了 (怎么可能？)，回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("超额支付，订单 id=" + orderId);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 创建及返回支付单Vo对象
        PaymentOrderVo paymentOrderVo = new PaymentOrderVo(paymentPo);
        return new APIReturnObject<>(paymentOrderVo);
    }

    /**
     * 服务 p4: 买家【根据订单号】查询自己的支付信息
     *
     * @author 苗新宇
     * Created at 05/12/2020 17:28
     * Created by Han Li at 05/12/2020 17:28
     * @param customerId
     * @param orderId
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    public APIReturnObject<?> getPaymentOrderByOrderId(Long customerId, Long orderId) {
        // 校验订单 id 是否存在 / 属于用户？
        // TODO - 我觉得，如果只要校验订单的话，不必要 get，过后写一个 count 函数到dao里面去。
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(orderId, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObject;
        }

        // 获取支付单列表
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByOrderId(orderId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }

        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        List<PaymentOrderVo> paymentOrderVos = paymentPoList.stream()
                .map(PaymentOrderVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentOrderVos);
    }

    /**
     * 服务 5: 管理员查询订单的支付信息 TODO - 这个等分单以后测试
     *
     * @author 苗新宇
     * Created at 05/12/2020 17:29
     * Created by 苗新宇 at 05/12/2020 17:29
     * @param shopId
     * @param orderId
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    public APIReturnObject<?> getOrderPaymentInfo(Long shopId, Long orderId) {
        //根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return orders;
        }
        //根据订单ID查询支付单信息,一个订单ID可能对应多个支付单
        APIReturnObject<List<PaymentPo>> paymentPoList = paymentDao.getPaymentOrderByOrderId(orderId);
        //定义PaymentOrderVo集合
        List<PaymentOrderVo> paymentOrderVoList;
        //将PaymentPo【Po对象】集合转换成PaymentOrderVo【Vo对象】集合
        paymentOrderVoList = paymentPoList.getData().stream()
                .map(PaymentOrderVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentOrderVoList);
    }

    /**
     * 服务 6: 买家为售后单创建支付单
     * @param aftersaleId
     * @param paymentInfoVO
     * @return
     */
    public APIReturnObject<?> createPaymentBillForAftersaleOrder(Long aftersaleId, PaymentInfoVo paymentInfoVO) {
        // TODO - 检查一下，这张售后单可不可以创建支付单
        if (!afterSaleService.canAfterSaleCreatePayment(aftersaleId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.BAD_REQUEST, "这张售后单无效或无法创建支付单");
        }

        // 创建支付单Po对象
        PaymentPo paymentPo = new PaymentPo();
        // 将支付单的Po对象的amount、actualAmount值设为paymentInfoVO的price
        paymentPo.setActualAmount(paymentInfoVO.getPrice());
        paymentPo.setAmount(paymentInfoVO.getPrice());
        paymentPo.setOrderId(null);

        // 各种时间
        LocalDateTime nowTime = LocalDateTime.now();
        paymentPo.setPayTime(nowTime);
        paymentPo.setBeginTime(nowTime);
        paymentPo.setEndTime(nowTime);
        paymentPo.setGmtCreate(nowTime);
        paymentPo.setGmtModified(null);

        // 设置 aftersaleId
        paymentPo.setAftersaleId(aftersaleId);

        // 模拟支付平台：已经支付
        paymentPo.setState(PaymentStatus.PAID.getCode());
        paymentPo.setPaymentPattern(PayPattern.MOCK.getCode());

        // 将支付单Po对象插入数据库
        try {
            int response = paymentDao.addPaymentOrder(paymentPo);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 创建支付单Vo对象:
        PaymentOrderVo paymentOrderVo = new PaymentOrderVo(paymentPo);
        // 将Vo对象返回
        return new APIReturnObject<>(paymentOrderVo);
    }

    /**
     * 服务 p7: 买家【根据售后单号】查询自己的支付信息
     *
     * @param aftersaleId 售后单ID
     * @return
     */
    public APIReturnObject<?> getPaymentOrderByAftersaleId(Long aftersaleId) {
        // TODO - 获取售后单，检查是否属于买家

        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        List<PaymentOrderVo> paymentOrderVos = paymentPoList.stream()
                .map(PaymentOrderVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentOrderVos);
    }

    /**
     * 服务 p8: 管理员【根据售后单号】查询售后单的支付信息
     *
     * @param shopId      店铺ID
     * @param aftersaleId 售后单ID
     * @return
     */
    public APIReturnObject<?> getPaymentOrderInfo(Long shopId, Long aftersaleId) {
        // TODO - 获取售后单，检查是否属于店铺
        if (!afterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }
        // 根据售后单号查询支付单
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }
        List<PaymentPo> paymentPoList = returnObj.getData();

        List<PaymentOrderVo> paymentOrderVos;
        //【PO对象】转换成【VO对象】返回给controller层
        paymentOrderVos = paymentPoList.stream()
                .map(PaymentOrderVo::new)
                .collect(Collectors.toList());
        //返回支付单
        return new APIReturnObject<>(paymentOrderVos);
    }

    /**
     * 服务 p9: 管理员创建退款信息，需检查Payment是否是此商铺的payment
     *
     * @param shopId         店铺ID
     * @param paymentId      支付单ID
     * @param amount         退款金额
     * @return
     */
    public APIReturnObject<?> createRefund(Long shopId, Long paymentId, Long amount) {
        //在payment表中根据paymentId查询orderId
        APIReturnObject<PaymentPo> returnObj = paymentDao.getPaymentOrderByPaymentId(paymentId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }
        PaymentPo paymentPo = returnObj.getData();
        // 获取 payment 中记载的订单ID、售后单 id，抄录到 refund 当中
        Long orderId = paymentPo.getOrderId();
        // 获取售后单ID
        Long aftersaleId = paymentPo.getAftersaleId();
        if (orderId != null) {
            // 根据店铺ID和订单ID查询订单, 未查到则说明该订单ID不属于该店铺，返回404
            APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
            if (orders.getCode() != ResponseCode.OK) {
                logger.info("企图查询不属于此商铺的 payment，该 payment 所对应之【订单】号不属于此商铺, paymentId=" + paymentId);
                return orders;
            }
        } else if (aftersaleId != null) {
            // 查询售后 id 对应之售后单的信息是否是此商铺的
            if (!afterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
                logger.info("企图查询不属于此商铺的 payment，该 payment 所对应之【售后单】号不属于此商铺，paymentId=" + paymentId);
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
            }
        } else {
            // 订单、售后单均为空，则该 payment 是 dangling payment，打印出错信息并返回 500
            logger.error("查询到【无任何所属关系】的坏账 payment，该 payment 未有记录 订单号 及 售后单号，paymentId=" + paymentId);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 现在，已经可以证明该 payment 属于该店铺，可以创建售后单
        // 新建 refund PO对象
        RefundPo refundPo = new RefundPo();
        refundPo.setPaymentId(paymentId);
        refundPo.setOrderId(orderId);
        refundPo.setAftersaleId(aftersaleId);
        // 模拟支付环境的都是已经退款
        refundPo.setState(RefundStatus.ALREADY_REFUND.getCode());
        refundPo.setAmout(amount);
        refundPo.setGmtCreate(LocalDateTime.now());
        refundPo.setGmtModified(null);
        // 将退款单Po对象插入数据库
        try {
            int response = paymentDao.addRefund(refundPo);
            if (response <= 0) {
                logger.error("插入该退款单时，出现未指明的错误 (response <= 0)");
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 新建RefundVo对象
        RefundVo refundVo = new RefundVo(refundPo);
        // 将Vo对象返回
        return new APIReturnObject<>(refundVo);
    }

    /**
     * 服务 p10: 管理员【根据订单ID】查询订单的退款信息
     *
     * @param shopId  店铺ID
     * @param orderId 订单ID
     * @return APIReturnObject<List<RefundVo>>
     */
    public APIReturnObject<?> getRefundByOrderId(Long shopId, Long orderId) {
        // 根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
        if (orders.getCode() != ResponseCode.OK) {
            return orders;
        }
        // 根据订单号、商铺号查询退款单
        APIReturnObject<RefundPo> refundPo = paymentDao.getRefundByOrderId(orderId);
        if (refundPo.getCode() != ResponseCode.OK) {
            return refundPo;
        }
        // 返回退款单
        RefundPo po = refundPo.getData();
        if (po == null) {
            return refundPo;
        }
        return new APIReturnObject<>(new RefundVo(po));
    }

    /**
     * 服务 p11: 管理员【根据售后单ID】查询订单的退款信息
     *
     * @param shopId      店铺ID
     * @param aftersaleId 售后单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getRefundByAftersaleId(Long shopId, Long aftersaleId) {
        // TODO - 检查售后单是否属于店铺
        if (!afterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }
        // 根据售后单号查询退款单
        APIReturnObject<RefundPo> refundPo = paymentDao.getRefundByAfterSaleId(aftersaleId);
        if (refundPo.getCode() != ResponseCode.OK) {
            return refundPo;
        }
        // 返回退款单
        RefundPo po = refundPo.getData();
        if (po == null) {
            return refundPo;
        }
        return new APIReturnObject<>(new RefundVo(po));
    }

    /**
     * 服务 p12: 买家【根据订单ID】查询订单的退款信息
     *
     * @param orderId 订单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getCustomerRefundByOrderId(Long customerId, Long orderId) {
        // 根据买家ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, customerId, null, false);
        if (orders.getCode() != ResponseCode.OK) {
            return orders;
        }
        // 根据订单号查询退款单
        APIReturnObject<RefundPo> refundPo = paymentDao.getRefundByOrderId(orderId);
        if (refundPo.getCode() != ResponseCode.OK) {
            return refundPo;
        }
        // 返回退款单
        RefundPo po = refundPo.getData();
        if (po == null) {
            return refundPo;
        }
        return new APIReturnObject<>(new RefundVo(po));
    }

    /**
     * 服务 p13: 买家【根据售后单ID】查询订单的退款信息
     *
     * @param aftersaleId 售后单ID
     * @return APIReturnObject<RefundVo>
     */
    public APIReturnObject<?> getCustomerRefundByAftersaleId(Long customerId, Long aftersaleId) {
        // TODO - 检查售后单是否属于买家
        if (!afterSaleService.isAfterSaleBelongsToCustomer(aftersaleId, customerId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST);
        }
        // 根据售后单号查询退款单
        APIReturnObject<RefundPo> refundPo = paymentDao.getRefundByAfterSaleId(aftersaleId);
        if (refundPo.getCode() != ResponseCode.OK) {
            return refundPo;
        }
        // 返回退款单
        RefundPo po = refundPo.getData();
        if (po == null) {
            return refundPo;
        }
        return new APIReturnObject<>(new RefundVo(po));
    }
}
