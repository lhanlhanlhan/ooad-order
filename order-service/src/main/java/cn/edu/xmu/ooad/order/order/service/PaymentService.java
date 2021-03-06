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
import cn.edu.xmu.ooad.order.require.models.AfterSaleInfo;
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
        Order simpleOrder = orderDao.getSimpleOrder(orderId, false);
        if (simpleOrder == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本人的订单
        if (simpleOrder.getCustomerId() != null && !simpleOrder.getCustomerId().equals(customerId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 看看现在的状态能不能支付及需要支付多少
        long shallPayPrice = simpleOrder.shallPayPrice();
        if (shallPayPrice == 0) {
            return new APIReturnObject<>(ResponseCode.ORDER_STATENOTALLOW);
        } else if (shallPayPrice <= -1) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 看看有没有超额支付，要超额支付的话，不让支付
        if (paymentNewVo.getPrice() > shallPayPrice) {
            // 企图超额支付
            return new APIReturnObject<>(ResponseCode.ORDER_STATENOTALLOW, "支付超额");
        }

        // 创建支付单Po对象:
        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setOrderId(orderId);

        // 钱
        paymentPo.setActualAmount(paymentNewVo.getPrice()); // TODO - 你前端只传了1个参数，让我咋区分ActualPrice和Price？？
        paymentPo.setAmount(paymentNewVo.getPrice());

        // 设各种时间
        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
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
            int response = paymentDao.addPayment(paymentPo);
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
                editPo.setSubstate(simpleOrder.getSubstate().getCode());
            }
            // 2. 分单 TODO - 优化分单过程
            // 查询夫订单
            Order order = orderDao.getOrder(orderId, false);
            if (order == null) {
                // 分单时查询订单失败，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("分单时查询订单失败! orderId=" + orderId);
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            // 拆分订单
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
            boolean ok = orderDao.modifyOrder(editPo);
            if (!ok) {
                // 改变状态失败，回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.INTERNAL_SERVER_ERR);
            }
        }

        // 创建及返回支付单Vo对象
        PaymentVo paymentVo = new PaymentVo(paymentPo);
        return new APIReturnObject<>(HttpStatus.CREATED, ResponseCode.OK, paymentVo);
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
        Order order = orderDao.getSimpleOrder(orderId, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 获取支付单列表
        List<PaymentPo> paymentPoList = paymentDao.getPaymentByOrderId(orderId);
        if (paymentPoList == null) {
            return null;
        }

        //【PO对象】转换成【VO对象】返回给controller层
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
        Order trueOrder = orderDao.getSimpleOrder(orderId, true);
        if (trueOrder == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (trueOrder.getShopId() == null || !shopId.equals(trueOrder.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 看看售后单是不是本店铺的
        if (shopId != 0 && !shopId.equals(trueOrder.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 获取支付单列表
        List<PaymentPo> paymentPoList = paymentDao.getPaymentByOrderId(orderId);
        if (paymentPoList == null) {
            return null;
        }
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentVo> paymentVos = paymentPoList.stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(paymentVos);
    }

    /**
     * 服务 6: 买家为售后单创建支付单
     */
    public APIReturnObject<?> createPaymentForAftersaleOrder(Long aftersaleId, Long price) {
        // AFTERSALE_ID

        // 创建支付单Po对象
        PaymentPo paymentPo = new PaymentPo();
        // 钱
        paymentPo.setActualAmount(price);
        paymentPo.setAmount(price);
        paymentPo.setOrderId(null);

        // 各种时间
        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());
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
            paymentDao.addPayment(paymentPo);
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
        AfterSaleInfo afterSaleInfo;
        try {
            afterSaleInfo = iAfterSaleService.getAfterSaleInfo(aftersaleId);
        } catch (Exception e) {
            logger.error("无法联系售后模块，错误：" + e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系售后模块：" + e.getMessage());
        }
        if (afterSaleInfo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (!customerId.equals(afterSaleInfo.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 給據查找
        List<PaymentPo> paymentPoList = paymentDao.getPaymentByAfterSaleId(aftersaleId);
        if (paymentPoList == null) {
            return null;
        }
        //【PO对象】转换成【VO对象】返回给controller层
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
        AfterSaleInfo afterSaleInfo;
        try {
            afterSaleInfo = iAfterSaleService.getAfterSaleInfo(aftersaleId);
        } catch (Exception e) {
            logger.error("无法联系售后模块，错误：" + e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系售后模块：" + e.getMessage());
        }
        if (afterSaleInfo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (!shopId.equals(afterSaleInfo.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 看看售后单是不是本店铺的
        if (shopId != 0 && !shopId.equals(afterSaleInfo.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 給據查找
        List<PaymentPo> paymentPoList = paymentDao.getPaymentByAfterSaleId(aftersaleId);
        if (paymentPoList == null) {
            return null;
        }
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentVo> paymentVos = paymentPoList.stream()
                .map(PaymentVo::new)
                .collect(Collectors.toList());
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
        PaymentPo paymentPo = paymentDao.getPayment(paymentId);
        if (paymentPo == null) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 获取 payment 中记载的订单ID、售后单 id，抄录到 refund 当中
        Long orderId = paymentPo.getOrderId();
        // 获取售后单ID
        Long aftersaleId = paymentPo.getAftersaleId();
        if (orderId != null) {
            // 根据店铺ID和订单ID查询订单, 未查到则说明该订单ID不属于该店铺，返回404
            Order order = orderDao.getSimpleOrder(orderId, true);
            if (order == null) {
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            // 看看售后单是不是本店铺的
            if (shopId != 0 && !shopId.equals(order.getShopId())) {
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
        } else if (aftersaleId != null) {
            // 查询售后 id 对应之售后单的信息是否是此商铺的
            AfterSaleInfo afterSaleInfo;
            try {
                afterSaleInfo = iAfterSaleService.getAfterSaleInfo(aftersaleId);
            } catch (Exception e) {
                logger.error("无法联系售后模块，错误：" + e.getMessage());
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系售后模块：" + e.getMessage());
            }
            if (afterSaleInfo == null) {
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            // 看看售后单是不是本店铺的
            if (shopId != 0 && !shopId.equals(afterSaleInfo.getShopId())) {
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
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
        refundPo.setGmtCreate(Accessories.secondTime(LocalDateTime.now()));
        refundPo.setGmtModified(Accessories.secondTime(LocalDateTime.now()));
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
        // 校验订单 id 是否存在 / 属于用户？
        Order order = orderDao.getSimpleOrder(orderId, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 看看售后单是不是本店铺的
        if (shopId != 0 && !shopId.equals(order.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 根据订单号 查询退款单
        List<RefundPo> refundPos = paymentDao.getRefundByOrderId(orderId);
        if (refundPos == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (refundPos.size() == 0) {
            return new APIReturnObject<>(refundPos);
        }
        List<RefundVo> refundVos = refundPos.stream()
                .map(RefundVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(refundVos);
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
        AfterSaleInfo afterSaleInfo;
        try {
            afterSaleInfo = iAfterSaleService.getAfterSaleInfo(aftersaleId);
        } catch (Exception e) {
            logger.error("无法联系售后模块，错误：" + e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系售后模块：" + e.getMessage());
        }
        if (afterSaleInfo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 看看售后单是不是本店铺的
        if (shopId != 0 && !shopId.equals(afterSaleInfo.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 根据售后单号查询退款单
        RefundPo refundPo = paymentDao.getRefundByAfterSaleId(aftersaleId);
        if (refundPo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        return new APIReturnObject<>(new RefundVo(refundPo));
    }

    /**
     * 服务 p12: 买家【根据订单ID】查询订单的退款信息
     *
     * @param orderId 订单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getCustomerRefundByOrderId(Long customerId, Long orderId) {
        // 校验订单 id 是否存在 / 属于用户？
        Order order = orderDao.getSimpleOrder(orderId, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (order.getCustomerId() == null || !customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 根据订单号 查询退款单
        List<RefundPo> refundPos = paymentDao.getRefundByOrderId(orderId);
        if (refundPos == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (refundPos.size() == 0) {
            return new APIReturnObject<>(refundPos);
        }
        List<RefundVo> refundVos = refundPos.stream()
                .map(RefundVo::new)
                .collect(Collectors.toList());
        return new APIReturnObject<>(refundVos);
    }

    /**
     * 服务 p13: 买家【根据售后单ID】查询订单的退款信息
     *
     * @param aftersaleId 售后单ID
     * @return APIReturnObject<RefundVo>
     */
    public APIReturnObject<?> getCustomerRefundByAftersaleId(Long customerId, Long aftersaleId) {
        // 其他模塊，检查售后单是否属于买家
        AfterSaleInfo afterSaleInfo;
        try {
            System.out.println("开始查询售后单");
            afterSaleInfo = iAfterSaleService.getAfterSaleInfo(aftersaleId);
            System.out.println("查询到售后单");
        } catch (Exception e) {
            logger.error("无法联系售后模块，错误：" + e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "无法联系售后模块：" + e.getMessage());
        }
        if (afterSaleInfo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        logger.info(customerId + ": " + afterSaleInfo);
        if (!customerId.equals(afterSaleInfo.getCustomerId())) {
            logger.info("企图查询不属于此买家的 payment，该 payment 所对应之【售后单】号不属于此买家，afterSaleId=" + aftersaleId + " customerId=" + afterSaleInfo.getCustomerId());
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // CaiXinLuTest: [即使 CustomerID 跟用户一致，但如果 OrderID 有，那就要检查 OrderID 的所属] 20/12/2020
        if (afterSaleInfo.getOrderId() != null) {
            Order order = orderDao.getSimpleOrder(afterSaleInfo.getOrderId(), false);
            if (order == null) {
                // 这张订单不存在，返回 404
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if (!customerId.equals(order.getCustomerId())) {
                return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
        }
        // 根据售后单号查询退款单
        RefundPo refundPo = paymentDao.getRefundByAfterSaleId(aftersaleId);
        if (refundPo == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        return new APIReturnObject<>(new RefundVo(refundPo));
    }
}

