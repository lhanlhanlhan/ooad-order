package cn.edu.xmu.oomall.order.dao;

import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.mapper.PaymentPoMapper;
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

    // 邱明规定的 Date Formatter
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    /**
     * 根据订单号查询支付单
     *
     * @param orderId 订单号
     * @return
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

        return new APIReturnObject<>(paymentSimplePoList);
    }

    /**
     * 插入支付单
     * @param po 支付单对象 po
     * @return
     */
    public int addPaymentOrder(PaymentPo po){return paymentPoMapper.insert(po);}
}
