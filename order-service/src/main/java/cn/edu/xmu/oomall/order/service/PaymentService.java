package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.PaymentDao;
import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.PaymentOrder;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import cn.edu.xmu.oomall.order.model.vo.PaymentInfoVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentOrderVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentStatusVo;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import org.apache.tomcat.jni.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        paymentPo.setState((byte) PaymentStatus.PENDING_PAY.getCode());
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
        paymentOrderVo.setState((byte) PaymentStatus.PENDING_PAY.getCode());
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
        paymentPo.setState((byte) PaymentStatus.PENDING_PAY.getCode());
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
        paymentOrderVo.setState((byte) PaymentStatus.PENDING_PAY.getCode());
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
}

