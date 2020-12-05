package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.dao.PaymentDao;
import cn.edu.xmu.oomall.order.enums.*;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.bo.PaymentOrder;
import cn.edu.xmu.oomall.order.model.po.OrderEditPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import cn.edu.xmu.oomall.order.model.po.RefundPo;
import cn.edu.xmu.oomall.order.model.vo.PaymentInfoVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentOrderVo;
import cn.edu.xmu.oomall.order.model.vo.RefundAmountVo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    //买家为售后单创建支付单

    //TODO - 从其他模块获取订单id？暂设为null
    public APIReturnObject<?> createPaymentBillForAftersaleOrder(
            Long aftersaleId,
            PaymentInfoVo paymentInfoVO) {
        //创建支付单Po对象
        PaymentPo paymentPo = new PaymentPo();
        //将支付单的Po对象的amount、actualAmount值设为paymentInfoVO的price
        paymentPo.setActualAmount(paymentInfoVO.getPrice());
        paymentPo.setAmount(paymentInfoVO.getPrice());
        //赋值订单号
        paymentPo.setOrderId(null);
        //将支付时间设置为当前系统时间
        paymentPo.setPayTime(LocalDateTime.now());
        //将开始时间和结束时间设置为系统当前时间
        paymentPo.setBeginTime(LocalDateTime.now());
        paymentPo.setEndTime(LocalDateTime.now());
        paymentPo.setGmtCreate(LocalDateTime.now());
        paymentPo.setGmtModified(null);
        //设置aftersaleId
        paymentPo.setAftersaleId(aftersaleId);
        //将支付状态一律赋值为1：待支付
        paymentPo.setState(PaymentStatus.PENDING_PAY.getCode());
        //将paymentPattern赋值为（byte)1
        paymentPo.setPaymentPattern(PayPattern.MOCK.getCode());
        //将支付单Po对象插入数据库
        try {
            int response = paymentDao.addPaymentOrder(paymentPo);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }


        //创建支付单Vo对象:
        PaymentOrderVo paymentOrderVo = new PaymentOrderVo();
        //将创建的Po对象的id赋值给VO对象的id值
        paymentOrderVo.setId(paymentPo.getId());
        //将参数paymentInfoVo的price值赋值给paymentOrderVo（支付单VO）中的amount
        paymentOrderVo.setAmount(paymentInfoVO.getPrice());
        paymentOrderVo.setActualAmount(paymentInfoVO.getPrice());
        //赋值订单号
        paymentOrderVo.setOrderId(null);
        //将支付时间设置为当前系统时间
        paymentOrderVo.setPayTime(LocalDateTime.now());
        //将开始时间和结束时间设置为系统当前时间
        paymentOrderVo.setBeginTime(LocalDateTime.now());
        paymentOrderVo.setEndTime(LocalDateTime.now());
        paymentOrderVo.setGmtCreate(LocalDateTime.now());
        paymentOrderVo.setGmtModified(null);
        //将支付状态一律赋值为0：待支付
        paymentOrderVo.setState(PaymentStatus.PENDING_PAY.getCode());
        //将paymentPattern赋值为001
        paymentOrderVo.setPaymentPattern("001");
        //将Vo对象返回
        return new APIReturnObject<>(paymentOrderVo);
    }

    /**
     * 07: 买家【根据售后单号】查询自己的支付信息 【Done】
     *
     * @param aftersaleId 售后单ID
     * @return
     */
    public APIReturnObject<?> getPaymentOrderByAftersaleId(Long aftersaleId) {
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        List<PaymentOrderVo> paymentOrderVos;
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        paymentOrderVos = paymentPoList.stream()
                .map(PaymentOrderVo::new)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        //用Map封装
        map.put("list", paymentOrderVos);
        return new APIReturnObject<>(map);
    }

    /**
     * 08: 管理员【根据售后单号】查询售后单的支付信息【done】
     *
     * @param shopId      店铺ID
     * @param aftersaleId 售后单ID
     * @return
     */
    public APIReturnObject<?> getPaymentOrderInfo(Long shopId, Long aftersaleId) {
        //根据售后单号查询支付单
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        List<PaymentPo> paymentPoList = returnObj.getData();
        if (paymentPoList.size() != 1) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        PaymentPo paymentPo = paymentPoList.get(0);
        //根据支付单查询到订单ID
        Long orderId = paymentPo.getOrderId();
        //根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return orders;
        }
        //返回支付单
        return new APIReturnObject<>(paymentPo);
    }


    /**
     * 09. 管理员创建退款信息，需检查Payment是否是此商铺的payment
     *
     * @param shopId         店铺ID
     * @param paymentId      支付单ID
     * @param refundAmountVo 退款金额VO
     * @return
     */
    // TODO - PO对象的billId如何设置？
    public APIReturnObject<?> createRefund(Long shopId, Long paymentId, RefundAmountVo refundAmountVo) {
        //在payment表中根据paymentId查询orderId
        APIReturnObject<PaymentPo> returnObj = paymentDao.getPaymentOrderByPaymentId(paymentId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        //获取订单ID
        Long orderId = returnObj.getData().getOrderId();
        //获取售后单ID
        Long aftersaleId = returnObj.getData().getAftersaleId();
        //根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return orders;
        }
        //新建refund PO对象
        RefundPo refundPo = new RefundPo();
        refundPo.setPaymentId(paymentId);
        //新建退款单的状态一律设置为NOT_REFUND：未退款
        refundPo.setState(RefundStatus.NOT_REFUND.getCode());
        refundPo.setAmout(refundAmountVo.getAmount());
        refundPo.setGmtCreate(LocalDateTime.now());
        refundPo.setGmtModified(null);
        //将退款单Po对象插入数据库
        try {
            int response = paymentDao.addRefund(refundPo);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        //新建RefundVo对象
        RefundVo refundVo = new RefundVo();
        refundVo.setId(refundPo.getId());
        refundVo.setPaymentId(paymentId);
        refundVo.setAmount(refundAmountVo.getAmount());
        //新建退款单的状态一律设置为NOT_REFUND：未退款
        refundVo.setState(RefundStatus.NOT_REFUND.getCode());
        refundVo.setGmtCreate(LocalDateTime.now());
        refundVo.setGmtModified(null);
        refundVo.setOrderId(orderId);
        refundVo.setAftersaleId(aftersaleId);
        //将Vo对象返回
        return new APIReturnObject<>(refundVo);
    }

    /**
     * 10. 管理员【根据订单ID】查询订单的退款信息
     *
     * @param shopId  店铺ID
     * @param orderId 订单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getRefundByOrderId(Long shopId, Long orderId) {
        //根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<Order> orders = orderDao.getSimpleOrder(orderId, null, shopId, true);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return orders;
        }
        //根据订单ID查询支付单信息,一个订单ID可能对应多个支付单
        APIReturnObject<List<PaymentPo>> paymentPoList = paymentDao.getPaymentOrderByOrderId(orderId);
        //取出PaymentPo List支付单集合
        List<PaymentPo> pos = paymentPoList.getData();
        //进行判断，个数小于1即为未找到
        if (pos.size() < 1) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, orders.getCode(), orders.getErrMsg());
        }
        //定义List<RefundPo> 退款单List
        List<RefundPo> refundPo = new ArrayList<>();
        //根据支付单号查询退款单并添加到退款单List
        for (PaymentPo po : pos) {
            refundPo.add(paymentDao.getRefundByPaymentId(po.getId()).getData());
        }
        //定义List<RefundVo>集合
        List<RefundVo> refundVo = new ArrayList<>();
        //将Po对象转换成Vo对象：
        for (RefundPo po : refundPo) {
            RefundVo refundVo1 = new RefundVo();
            refundVo1.setId(po.getId());
            refundVo1.setPaymentId(po.getPaymentId());
            refundVo1.setGmtCreate(po.getGmtCreate());
            refundVo1.setGmtModified(po.getGmtModified());
            refundVo1.setState(po.getState());
            refundVo1.setAmount(po.getAmout());
            refundVo1.setOrderId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getOrderId());
            refundVo1.setAftersaleId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getAftersaleId());
            refundVo.add(refundVo1);
        }
        return new APIReturnObject<>(refundVo);
    }

    /**
     * 11. 管理员【根据售后单ID】查询订单的退款信息
     *
     * @param shopId      店铺ID
     * @param aftersaleId 售后单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getRefundByAftersaleId(Long shopId, Long aftersaleId) {
        //【在payment表中】根据售后单号查询支付单
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        List<PaymentPo> paymentPoList = returnObj.getData();
        //一个支付单可对应多个退款单
        //定义List<RefundPo> 退款单List
        List<RefundPo> refundPo = new ArrayList<>();
        //根据支付单号查询退款单并添加到退款单List
        for (PaymentPo po : paymentPoList) {
            refundPo.add(paymentDao.getRefundByPaymentId(po.getId()).getData());
        }
        //定义List<RefundVo>集合
        List<RefundVo> refundVo = new ArrayList<>();
        //将Po对象转换成Vo对象：
        for (RefundPo po : refundPo) {
            RefundVo refundVo1 = new RefundVo();
            refundVo1.setId(po.getId());
            refundVo1.setPaymentId(po.getPaymentId());
            refundVo1.setGmtCreate(po.getGmtCreate());
            refundVo1.setGmtModified(po.getGmtModified());
            refundVo1.setState(po.getState());
            refundVo1.setAmount(po.getAmout());
            refundVo1.setOrderId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getOrderId());
            refundVo1.setAftersaleId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getAftersaleId());
            refundVo.add(refundVo1);
        }
        return new APIReturnObject<>(refundVo);

    }

    /**
     * 12. 买家【根据订单ID】查询订单的退款信息
     *
     * @param orderId 订单ID
     * @return APIReturnObject<List < RefundVo>>
     */
    public APIReturnObject<?> getSelfRefundByOrderId(Long orderId) {
        //根据订单Id查询支付单Id
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByOrderId(orderId);
        List<PaymentPo> paymentPoList = returnObj.getData();
        if (returnObj.getCode() != ResponseCode.OK || paymentPoList.size() < 0) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        //根据支付单Id查询退款单：
        //定义List<RefundPo> 退款单List
        List<RefundPo> refundPo = new ArrayList<>();
        //根据支付单号查询退款单并添加到退款单List
        for (PaymentPo po : paymentPoList) {
            refundPo.add(paymentDao.getRefundByPaymentId(po.getId()).getData());
        }
        //定义List<RefundVo>集合
        List<RefundVo> refundVo = new ArrayList<>();
        //将Po对象转换成Vo对象：
        for (RefundPo po : refundPo) {
            RefundVo refundVo1 = new RefundVo();
            refundVo1.setId(po.getId());
            refundVo1.setPaymentId(po.getPaymentId());
            refundVo1.setGmtCreate(po.getGmtCreate());
            refundVo1.setGmtModified(po.getGmtModified());
            refundVo1.setState(po.getState());
            refundVo1.setAmount(po.getAmout());
            refundVo1.setOrderId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getOrderId());
            refundVo1.setAftersaleId(paymentDao.getPaymentOrderByPaymentId(po.getPaymentId()).getData().getAftersaleId());
            refundVo.add(refundVo1);
        }
        return new APIReturnObject<>(refundVo);
    }

    /**
     * 13. 买家【根据售后单ID】查询订单的退款信息
     *
     * @param aftersaleId 售后单ID
     * @return APIReturnObject<RefundVo>
     */
    public APIReturnObject<?> getSelfRefundByAftersaleId(Long aftersaleId) {
        //【在payment表中】根据售后单id查询支付单id
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByAftersaleId(aftersaleId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        List<PaymentPo> paymentPoList = returnObj.getData();
        //一个退款单最多对应一个支付单
        if (paymentPoList.size() != 1) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //获取到支付单
        PaymentPo paymentPo = paymentPoList.get(0);

        //根据支付单ID查询退款单
        APIReturnObject<RefundPo> returnObject = paymentDao.getRefundByPaymentId(paymentPo.getId());
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        RefundPo refundPo = returnObject.getData();
        //构建VO对象进行封装
        RefundVo refundVo = new RefundVo();
        refundVo.setId(refundPo.getId());
        refundVo.setPaymentId(refundPo.getPaymentId());
        refundVo.setGmtCreate(refundPo.getGmtCreate());
        refundVo.setGmtModified(refundPo.getGmtModified());
        refundVo.setState(refundPo.getState());
        refundVo.setAmount(refundPo.getAmout());
        refundVo.setOrderId(paymentPo.getOrderId());
        refundVo.setAftersaleId(aftersaleId);
        return new APIReturnObject<>(refundVo);
    }
}

