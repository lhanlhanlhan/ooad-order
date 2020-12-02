package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.OrderSimplePoMapper;
import cn.edu.xmu.oomall.order.mapper.PaymentPoMapper;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePoExample;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import cn.edu.xmu.oomall.order.model.po.PaymentPoExample;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 支付 Dao
 *
 * @author 苗新宇
 * Created at 30/11/2020 21:53
 * Modified by  苗新宇 at  30/11/2020 21:53
 */
@Repository
public class PaymentDao {

    // 日志记录器
    private static Logger logger = LoggerFactory.getLogger(PaymentDao.class);

    @Autowired
    private PaymentPoMapper paymentPoMapper;

    @Autowired
    private OrderSimplePoMapper orderSimplePoMapper;
    // 邱明规定的 Date Formatter
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    /**
     * 根据订单号查询支付单
     *
     * @param orderId 订单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public APIReturnObject<List<PaymentPo>> getPaymentOrderByOrderId(Long orderId) {
        //创建PoExample对象，以实现根据订单号orderId查询支付单
        PaymentPoExample example = new PaymentPoExample();
        PaymentPoExample.Criteria criteria = example.createCriteria();
        if (orderId != null) {
            criteria.andOrderIdEqualTo(orderId);
        }
        //执行查询
        List<PaymentPo> paymentSimplePoList;
        try {
            paymentSimplePoList = paymentPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

        return new APIReturnObject(paymentSimplePoList);
    }

    /**
     * 插入支付单
     *
     * @param po 支付单对象 po
     * @return
     */
    public int addPaymentOrder(PaymentPo po) {
        return paymentPoMapper.insert(po);
    }

    /**
     *
     * @param aftersaleId 售后单号
     * @return APIReturnObject<PaymentPo>
     */
    public  APIReturnObject<PaymentPo> findOrderIdByAftersaleId(Long aftersaleId){
        //创建PoExample对象，以实现根据售后单号aftersaleId查询支付单
        PaymentPoExample poExample=new PaymentPoExample();
        PaymentPoExample.Criteria criteria=poExample.createCriteria();
        if(aftersaleId!=null){
            criteria.andAftersaleIdEqualTo(aftersaleId);
        }
        //执行查询
        List<PaymentPo> paymentSimplePoList;
        try {
            paymentSimplePoList = paymentPoMapper.selectByExample(poExample);
        } catch (Exception e) {
            //数据库错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject(paymentSimplePoList);
    }

    /**
     * 根据售后单号查询支付单
     *
     * @param aftersaleId 订单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public APIReturnObject<List<PaymentPo>> getPaymentOrderByAftersaleId(Long aftersaleId) {
        //创建PoExample对象，以实现根据订单号orderId查询支付单
        PaymentPoExample example = new PaymentPoExample();
        PaymentPoExample.Criteria criteria = example.createCriteria();
        if (aftersaleId != null) {
            criteria.andAftersaleIdEqualTo(aftersaleId);
        }
        //执行查询
        List<PaymentPo> paymentSimplePoList;
        try {
            paymentSimplePoList = paymentPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new APIReturnObject<>(paymentSimplePoList);
    }

    /**
     * 根据订单号以及售后单号查询支付单
     * @param orderId 订单号
     * @param aftersaleId 售后单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public APIReturnObject<PaymentPo> getPaymentOrderByAftersaleIdAndOrderId(Long orderId,Long aftersaleId) {
        //创建PoExample对象，以实现根据订单号orderId查询支付单
        PaymentPoExample example = new PaymentPoExample();
        PaymentPoExample.Criteria criteria = example.createCriteria();
        if (aftersaleId != null&&orderId==null) {
            criteria.andAftersaleIdEqualTo(aftersaleId);
            criteria.andOrderIdEqualTo(orderId);
        }
        //执行查询
        List<PaymentPo> paymentSimplePoList;
        try {
            paymentSimplePoList = paymentPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

        return new APIReturnObject(paymentSimplePoList);
    }

    /**
     * 根据店铺ID以及订单号查询订单
     * @param shopId 店铺ID
     * @param orderId 订单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public APIReturnObject<OrderSimplePo> getOrderByShopIdAndOrderId(Long shopId,Long orderId) {
        //创建Example对象，以实现根据店铺ID以及订单号查询订单
        OrderSimplePoExample example = new OrderSimplePoExample();
        OrderSimplePoExample.Criteria criteria = example.createCriteria();
        if (shopId != null&&orderId==null) {
            criteria.andShopIdEqualTo(shopId);
            criteria.andIdEqualTo(orderId);
        }
        //执行查询
        List<OrderSimplePo> orderSimplePos;
        try {
            orderSimplePos = orderSimplePoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.info(e.getMessage());
            return new APIReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

        return new APIReturnObject(orderSimplePos);
    }
}
