package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.model.vo.PayPatternsVo;
import cn.edu.xmu.oomall.order.model.vo.PaymentInfoVO;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付服务
 * @author 苗新宇
 * Created at 27/11/2020 8:45
 * Modified by  苗新宇 at  27/11/2020 8:45
 */
@Service
public class PaymentService {

    @Autowired
    private static final Logger logger= LoggerFactory.getLogger(PaymentService.class);
    /**
     * 服务 o3：买家为订单创建支付单
     *
     * @return APIReturnObject
     */
    public APIReturnObject<?> createPaymentOrder(Long customerId,
                                                 Long orderId,
                                                 PaymentInfoVO paymentInfoVO){


        //创建支付单
        return null;
    }
}
