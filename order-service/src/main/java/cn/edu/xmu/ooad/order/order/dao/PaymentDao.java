package cn.edu.xmu.ooad.order.order.dao;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.order.mapper.PaymentPoMapper;
import cn.edu.xmu.ooad.order.order.mapper.RefundPoMapper;
import cn.edu.xmu.ooad.order.order.model.po.PaymentPo;
import cn.edu.xmu.ooad.order.order.model.po.PaymentPoExample;
import cn.edu.xmu.ooad.order.order.model.po.RefundPo;
import cn.edu.xmu.ooad.order.order.model.po.RefundPoExample;
import cn.edu.xmu.ooad.util.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

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
    private static final Logger logger = LoggerFactory.getLogger(PaymentDao.class);

    @Autowired
    private PaymentPoMapper paymentPoMapper;

    @Autowired
    private RefundPoMapper refundPoMapper;

    /* 支付单部分 */

    /**
     * 插入支付单
     *
     * @param po 支付单对象 po
     * @return
     */
    public int addPayment(PaymentPo po) {
        return paymentPoMapper.insert(po);
    }

    /**
     * 根据支付单号查询支付单 【只有1个】
     *
     * @param paymentId 支付单ID
     * @return PaymentPo 支付单PO对象
     */
    public PaymentPo getPayment(Long paymentId) {
        PaymentPo paymentPo;
        try {
            paymentPo = paymentPoMapper.selectByPrimaryKey(paymentId);
        } catch (Exception e) {
            // 数据库错误
            logger.error(e.getMessage());
            return null;
        }
        if (paymentPo == null) {
            // 未能查到
            return null;
        }
        return paymentPo;
    }

    /**
     * 根据订单号查询支付单【可能有多个，API接受多个】
     *
     * @param orderId 订单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public List<PaymentPo> getPaymentByOrderId(Long orderId) {
        // 创建PoExample对象，以实现根据订单号orderId查询支付单
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
            logger.error(e.getMessage());
            return null;
        }

        return paymentSimplePoList;
    }

    /**
     * 根据售后单号查询支付单【可能有多个，API接受多个】
     *
     * @param afterSaleId 订单号
     * @return APIReturnObject<PaymentPo> PO List
     */
    public List<PaymentPo> getPaymentByAfterSaleId(Long afterSaleId) {
        //创建PoExample对象，以实现根据订单号orderId查询支付单
        PaymentPoExample example = new PaymentPoExample();
        PaymentPoExample.Criteria criteria = example.createCriteria();
        if (afterSaleId != null) {
            criteria.andAftersaleIdEqualTo(afterSaleId);
        }
        //执行查询
        List<PaymentPo> paymentSimplePoList;
        try {
            paymentSimplePoList = paymentPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.error(e.getMessage());
            return null;
        }
        return paymentSimplePoList;
    }

    /* 退款单部分 */

    /**
     * 插入退款单
     *
     * @param po 退款单对象 Po
     * @return
     */
    public int addRefund(RefundPo po) {
        return refundPoMapper.insert(po);
    }

    /**
     * 根据订单号查询退款单【可能有多个】
     */
    public List<RefundPo> getRefundByOrderId(Long orderId) {
        RefundPoExample example = new RefundPoExample();
        RefundPoExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        //执行查询
        List<RefundPo> refundPoList;
        try {
            refundPoList = refundPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.error(e.getMessage());
            return null;
        }
        // 订单的退款单只能有0个/1个吗？就不能有多张吗？ 【邱明说只返回第一张 6/12/2020 11:27】
        // 邱明又改需求了，全数返回【18/12/2020 10:28】
        // 其他情况返回第一张
        return refundPoList;
    }

    /**
     * 根据售后单号查询退款单 根据支付单号查询退款单 【可能有多个，但API只要1个】
     */
    public RefundPo getRefundByAfterSaleId(Long afterSaleId) {
        RefundPoExample example = new RefundPoExample();
        RefundPoExample.Criteria criteria = example.createCriteria();
        criteria.andAftersaleIdEqualTo(afterSaleId);
        //执行查询
        List<RefundPo> refundPoList;
        try {
            refundPoList = refundPoMapper.selectByExample(example);
        } catch (Exception e) {
            //数据库错误
            logger.error(e.getMessage());
            return null;
        }
        // 售后单的退款单只能有0个/1个吗？就不能有多张吗？ 【邱明说只返回第一张 6/12/2020 11:27】
        if (refundPoList.size() > 1) {
            logger.info("发现多于1张退款单绑定在该【售后单】上, API只能返回一个！ afterSaleId=" + afterSaleId);
        } else if (refundPoList.size() == 0) {
            logger.debug("售后单的退款单为空：asId" + afterSaleId);
            // 返回 0 张
            return null;
        }
        // 其他情况返回第一张
        return refundPoList.get(0);
    }
}
