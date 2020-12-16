package cn.edu.xmu.ooad.order.order.service;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.dao.PaymentDao;
import cn.edu.xmu.ooad.order.order.enums.PayPattern;
import cn.edu.xmu.ooad.order.order.enums.PaymentStatus;
import cn.edu.xmu.ooad.order.order.enums.RefundStatus;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.po.OrderEditPo;
import cn.edu.xmu.ooad.order.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.order.model.po.RefundPo;
import cn.edu.xmu.ooad.order.order.model.vo.PaymentNewVo;
import cn.edu.xmu.ooad.order.order.model.vo.PaymentVo;
import cn.edu.xmu.ooad.order.order.model.vo.RefundVo;
import cn.edu.xmu.ooad.order.require.IAfterSaleService;
import cn.edu.xmu.ooad.util.ResponseCode;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
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

    @DubboReference(check = false)
    private IAfterSaleService iAfterSaleService;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private OrderDao orderDao;


    /**
     * 服务 p3：买家为订单创建支付单
     *
     * @return APIReturnObject
     * @author 苗新宇
     * Created at 30/11/2020 8:45
     * Modified by Han Li at 5/12/2020 17:08
     */
    @Transactional
    public APIReturnObject<?> createPayment(Long orderId, Long customerId, PaymentNewVo paymentNewVo) {
        // 校验订单 id 是否存在 / 属于用户？
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(orderId, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObject;
        }
        Order simpleOrder = returnObject.getData();

        // 看看现在的状态能不能支付及需要支付多少
        long shallPayPrice = simpleOrder.shallPayPrice();
        if (shallPayPrice == 0) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
        } else if (shallPayPrice <= -1) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 看看有没有超额支付，要超额支付的话，不让支付
        if (paymentNewVo.getPrice() > shallPayPrice) {
            // 企图超额支付
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.FIELD_NOTVALID, "支付超额");
        }

        // 创建支付单Po对象:
        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setOrderId(orderId);

        // 钱
        paymentPo.setActualAmount(paymentNewVo.getPrice()); // TODO - 你前端只传了1个参数，让我咋区分ActualPrice和Price？？
        paymentPo.setAmount(paymentNewVo.getPrice());

        // 设各种时间
        LocalDateTime nowTime = LocalDateTime.now();
        paymentPo.setPayTime(nowTime);
        paymentPo.setBeginTime(nowTime);
        paymentPo.setEndTime(nowTime);
        paymentPo.setGmtCreate(nowTime);
        paymentPo.setGmtModified(nowTime);

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

        // 判断是否足额支付，若已足额支付，更改订单状态，分单
        if (paymentNewVo.getPrice() == shallPayPrice) {
            // 触发足额支付动作
            simpleOrder.triggerPaid();
            // 更改订单状态
            OrderEditPo editPo = new OrderEditPo();
            editPo.setId(orderId);
            if (simpleOrder.getState() != null) {
                editPo.setState(simpleOrder.getState().getCode());
            }
            if (simpleOrder.getSubstate() != null) {
                editPo.setSubState(simpleOrder.getSubstate().getCode());
            }
            // 2. 分单 TODO - 优化分单过程
            // 查询夫订单
            APIReturnObject<Order> fullOrder = orderDao.getOrder(orderId, customerId, null, false);
            if (fullOrder.getCode() != ResponseCode.OK) {
                // 分单时查询订单失败，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("分单时查询订单失败! orderId=" + orderId);
                return fullOrder;
            }
            // 拆分订单
            Order order = fullOrder.getData();
            List<Order> rippedOrder = order.splitToOrders();
            if (rippedOrder == null) {
                // 无需分单，直接写入 shopId
                editPo.setShopId(order.getShopId());
            } else {
                // 需要分单，要将新订单写入数据库
                for (Order subOrder : rippedOrder) {
                    // 插入子订单
                    int insSubOrderRet = orderDao.addOrder(subOrder.getOrderPo());
                    if (insSubOrderRet != 1) {
                        // 插入错误
                        logger.error("插入子订单时错误！");
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "插入子订单时出错。");
                    }
                    // 修改订单订单项目的订单号为子订单的
                    for (OrderItem subOrderItem : subOrder.getOrderItemList()) {
                        int updateOrderItemRet = orderDao.modifyOrderItemOrderId(subOrderItem.getId(), subOrder.getId());
                        if (updateOrderItemRet != 0) {
                            // 无法修改子订单的订单号
                            logger.error("修改子订单的订单号时错误！");
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "修改子订单的订单号时错误。");
                        }
                    }
                }
            }

            // 写入订单变更
            APIReturnObject<?> modifyRet = orderDao.modifyOrder(editPo);
            if (modifyRet.getCode() != ResponseCode.OK) {
                // 改变状态失败，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return modifyRet;
            }
        }

        // 创建及返回支付单Vo对象
        PaymentVo paymentVo = new PaymentVo(paymentPo);
        return new APIReturnObject<>(paymentVo);
    }

    /**
     * 服务 p4: 买家【根据订单号】查询自己的支付信息
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author 苗新宇
     * Created at 05/12/2020 17:28
     * Created by Han Li at 05/12/2020 17:28
     */
    public APIReturnObject<?> getPaymentByOrderId(Long customerId, Long orderId) {
        // 校验订单 id 是否存在 / 属于用户？
        long countRes = orderDao.countOrders(orderId, customerId, null, false);
        if (countRes == 0) { // 查無此單
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (countRes == -1) { // 數據庫錯誤
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 获取支付单列表
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByOrderId(orderId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }

        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        List<PaymentVo> paymentVos = paymentPoList.stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentVos);
    }

    /**
     * 服务 5: 管理员查询订单的支付信息
     * <p>
     * TODO - 支付信息是綁定在父訂單上的馬？
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author 苗新宇
     * Created at 05/12/2020 17:29
     * Created by 苗新宇 at 05/12/2020 17:29
     */
    public APIReturnObject<?> getOrderPaymentInfo(Long shopId, Long orderId) {
        APIReturnObject<Order> order = orderDao.getSimpleOrder(orderId, null, null, true);
        if (order.getCode() != ResponseCode.OK) {
            return order;
        }
        Order trueOrder = order.getData();
        if (trueOrder == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (trueOrder.getShopId() == null || !trueOrder.getShopId().equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        //根据订单ID查询支付单信息,一个订单ID可能对应多个支付单
        APIReturnObject<List<PaymentPo>> paymentPoList = paymentDao.getPaymentOrderByOrderId(orderId);
        //定义PaymentOrderVo集合
        List<PaymentVo> paymentVoList;
        //将PaymentPo【Po对象】集合转换成PaymentOrderVo【Vo对象】集合
        paymentVoList = paymentPoList.getData().stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentVoList);
    }

    /**
     * 服务 6: 买家为售后单创建支付单
     */
    public APIReturnObject<?> createPaymentForAftersaleOrder(Long aftersaleId, Long price) {
        // 创建支付单Po对象
        PaymentPo paymentPo = new PaymentPo();
        // 钱
        paymentPo.setActualAmount(price);
        paymentPo.setAmount(price);
        paymentPo.setOrderId(null);

        // 各种时间
        LocalDateTime nowTime = LocalDateTime.now();
        paymentPo.setPayTime(nowTime);
        paymentPo.setBeginTime(nowTime);
        paymentPo.setEndTime(nowTime);
        paymentPo.setGmtCreate(nowTime);
        paymentPo.setGmtModified(nowTime);

        // 设置 aftersaleId
        paymentPo.setAftersaleId(aftersaleId);

        // 模拟支付平台：已经支付
        paymentPo.setState(PaymentStatus.PAID.getCode());
        paymentPo.setPaymentPattern(PayPattern.MOCK.getCode());

        // 将支付单Po对象插入数据库
        try {
            paymentDao.addPaymentOrder(paymentPo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 创建支付单Vo对象:
        PaymentVo paymentVo = new PaymentVo(paymentPo);
        // 将Vo对象返回
        return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, paymentVo);
    }

    /**
     * 服务 p7: 买家【根据售后单号】查询自己的支付信息
     *
     * @param aftersaleId 售后单ID
     */
    public APIReturnObject<?> getPaymentByAftersaleId(Long aftersaleId, Long customerId) {
        // 其他模塊获取售后单，检查是否属于买家
        if (!iAfterSaleService.isAfterSaleBelongsToCustomer(aftersaleId, customerId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 給據查找
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        List<PaymentVo> paymentVos = paymentPoList.stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentVos);
    }

    /**
     * 服务 p8: 管理员【根据售后单号】查询售后单的支付信息
     *
     * @param shopId      店铺ID
     * @param aftersaleId 售后单ID
     */
    public APIReturnObject<?> getPaymentInfo(Long shopId, Long aftersaleId) {
        if (!iAfterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 根据售后单号查询支付单
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObj;
        }
        List<PaymentPo> paymentPoList = returnObj.getData();

        List<PaymentVo> paymentVos;
        //【PO对象】转换成【VO对象】返回给controller层
        paymentVos = paymentPoList.stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
        //返回支付单
        return new APIReturnObject<>(paymentVos);
    }

    /**
     * 服务 p9: 管理员创建退款信息，需检查Payment是否是此商铺的payment
     *
     * @param shopId    店铺ID
     * @param paymentId 支付单ID
     * @param amount    退款金额
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
            if (!iAfterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
                logger.info("企图查询不属于此商铺的 payment，该 payment 所对应之【售后单】号不属于此商铺，paymentId=" + paymentId);
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
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
        refundPo.setAmount(amount);
        refundPo.setGmtCreate(LocalDateTime.now());
        refundPo.setGmtModified(LocalDateTime.now());
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
        return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, refundVo);
    }

    /**
     * 服务 p10: 管理员【根据订单ID】查询订单的退款信息
     *
     * @param shopId  店铺ID
     * @param orderId 订单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getRefundByOrderId(Long shopId, Long orderId) {
        // 校验订单 id 是否存在 / 属于店鋪？
        long countRes = orderDao.countOrders(orderId, null, shopId, true);
        if (countRes == 0) { // 查無此單
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (countRes == -1) { // 數據庫錯誤
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
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
        // 其他模塊检查售后单是否属于店铺
        if (!iAfterSaleService.isAfterSaleBelongsToShop(aftersaleId, shopId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
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
        // 校验订单 id 是否存在 / 属于用户？
        long countRes = orderDao.countOrders(orderId, customerId, null, false);
        if (countRes == 0) { // 查無此單
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (countRes == -1) { // 數據庫錯誤
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
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
        // 其他模塊，检查售后单是否属于买家
        if (!iAfterSaleService.isAfterSaleBelongsToCustomer(aftersaleId, customerId)) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
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

