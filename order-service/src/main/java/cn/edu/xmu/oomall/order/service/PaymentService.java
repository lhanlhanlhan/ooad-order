package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.PaymentDao;
import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import cn.edu.xmu.oomall.order.enums.RefundStatus;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.PaymentOrder;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import cn.edu.xmu.oomall.order.model.po.RefundPo;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.Accessories;
import org.apache.tomcat.jni.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collector;
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


    /**
     * 服务 o3：买家为订单创建支付单
     *
     * @return APIReturnObject
     * @author 苗新宇
     * Created at 30/11/2020 8:45
     * Modified by  苗新宇 at  30/11/2020 8:45
     */
    public APIReturnObject<?> createPaymentOrder(
            Long orderId,
            PaymentInfoVo paymentInfoVO) {
        // TODO - 校验订单 id 是否存在？
        // TODO - aftersaleId何处取值？
        // TODO - paymentPattern在VO【PaymentOrderVo】和PO【PaymentPo】中的类型不一致，前者为String ，后者为Byte
        //创建支付单Po对象:
        PaymentPo paymentPo = new PaymentPo();
        //将支付单的Po对象的amount、actualAmount值设为paymentInfoVO的price
        paymentPo.setActualAmount(paymentInfoVO.getPrice());
        paymentPo.setAmount(paymentInfoVO.getPrice());
        //赋值订单号
        paymentPo.setOrderId(orderId);
        //将支付时间设置为当前系统时间
        paymentPo.setPayTime(LocalDateTime.now());
        //将开始时间和结束时间设置为系统当前时间
        paymentPo.setBeginTime(LocalDateTime.now());
        paymentPo.setEndTime(LocalDateTime.now());
        paymentPo.setGmtCreate(LocalDateTime.now());
        paymentPo.setGmtModified(null);
        //将aftersaleId设置为空
        paymentPo.setAftersaleId(null);
        //将支付状态一律赋值为1：待支付
        paymentPo.setState(PaymentStatus.PENDING_PAY.getCode());
        //将paymentPattern赋值为（byte)1
        paymentPo.setPaymentPattern((byte) 1);
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
        Long id = paymentPo.getId();

        //创建支付单Vo对象:
        PaymentOrderVo paymentOrderVo = new PaymentOrderVo();
        //将创建的Po对象的id赋值给VO对象的id值
        paymentOrderVo.setId(paymentPo.getId());
        //将参数paymentInfoVo的paymentPattern值赋值给paymentOrderVo（支付单VO）
        paymentOrderVo.setAmount(paymentInfoVO.getPrice());
        paymentOrderVo.setActualAmount(paymentInfoVO.getPrice());
        //赋值订单号
        paymentOrderVo.setOrderId(orderId);
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
     * 04: 买家【根据订单号】查询自己的支付信息
     *
     * @param orderId
     * @return
     */
    public APIReturnObject<?> getPaymentOrderByOrderId(Long orderId) {
        APIReturnObject<List<PaymentPo>> returnObj = paymentDao.getPaymentOrderByOrderId(orderId);
        if (returnObj.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
        }
        List<PaymentOrderVo> paymentOrderVos;
        //【PO对象】转换成【VO对象】返回给controller层
        List<PaymentPo> paymentPoList = returnObj.getData();
        paymentOrderVos = paymentPoList.stream()
                .map(PaymentOrder::new)
                .map(PaymentOrder::createSimpleVo)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        //用Map封装
        map.put("list", paymentOrderVos);
        return new APIReturnObject<>(map);
    }


    /**
     * 05. 管理员查询订单的支付信息
     *
     * @param shopId  店铺ID
     * @param orderId 订单ID
     * @return APIReturnObject<?>
     */
    public APIReturnObject<?> getOrderPaymentInfo(Long shopId, Long orderId) {
        //根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<OrderSimplePo> orders = paymentDao.getOrderByShopIdAndOrderId(shopId, orderId);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, orders.getCode(), orders.getErrMsg());
        }
        //根据订单ID查询支付单信息,一个订单ID可能对应多个支付单
        APIReturnObject<List<PaymentPo>> paymentPoList = paymentDao.getPaymentOrderByOrderId(orderId);
        //定义PaymentOrderVo集合
        List<PaymentOrderVo> paymentOrderVoList;
        //将PaymentPo【Po对象】集合转换成PaymentOrderVo【Vo对象】集合
        paymentOrderVoList = paymentPoList.getData().stream()
                .map(PaymentOrder::new)
                .map(PaymentOrder::createSimpleVo)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("list", paymentOrderVoList);
        return new APIReturnObject<>(map);
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
        paymentPo.setPaymentPattern((byte) 1);
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
                .map(PaymentOrder::new)
                .map(PaymentOrder::createSimpleVo)
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
        APIReturnObject<OrderSimplePo> orders = paymentDao.getOrderByShopIdAndOrderId(shopId, orderId);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, returnObj.getCode(), returnObj.getErrMsg());
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
        //检查Payment是否是此商铺的payment：根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<OrderSimplePo> orders = paymentDao.getOrderByShopIdAndOrderId(shopId, orderId);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, orders.getCode(), orders.getErrMsg());
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
        //检查订单是否属于此商铺：根据店铺ID和订单ID查询订单,未查到则说明该订单ID不属于该店铺，返回404
        APIReturnObject<OrderSimplePo> orders = paymentDao.getOrderByShopIdAndOrderId(shopId, orderId);
        if (orders.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject(HttpStatus.NOT_FOUND, orders.getCode(), orders.getErrMsg());
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

