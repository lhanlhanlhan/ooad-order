package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.PaymentDao;
import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.PaymentOrder;
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
import java.util.List;

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
     */
    public APIReturnObject<?> createPaymentOrder(Long customerId,
                                                 Long orderId,
                                                 PaymentInfoVo paymentInfoVO) {
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
        //将支付状态一律赋值为0：待支付
        paymentPo.setState((byte) PaymentStatus.PENDING_PAY.getCode());
        //将paymentPattern暂时赋值为（byte)0
        paymentPo.setPaymentPattern((byte) 0);
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
        //将参数paymentInfoVo的paymentPattern值赋值给paymentOrderVo（支付单VO）
        paymentOrderVo.setPaymentPattern(paymentInfoVO.getPaymentPattern());
        //将参数paymentInfoVo的price值赋值给paymentOrderVo（支付单VO）中的amount
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
        //将paymentPattern暂时赋值为null
        paymentOrderVo.setPaymentPattern(null);
        //将Vo对象返回
        return new APIReturnObject<>(paymentOrderVo);
    }
}
