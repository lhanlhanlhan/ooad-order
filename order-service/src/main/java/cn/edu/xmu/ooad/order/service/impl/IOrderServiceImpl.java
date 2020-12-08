package cn.edu.xmu.ooad.order.service.impl;

import cn.edu.xmu.ooad.order.client.IOrderService;
import cn.edu.xmu.ooad.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.service.OrderService;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 供其他两个模块调用的 订单服务 实现类
 *
 * @author Han Li
 * Created at 8/12/2020 10:14 上午
 * Modified by Han Li at 8/12/2020 10:14 上午
 */
@Service
public class IOrderServiceImpl implements IOrderService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(IOrderServiceImpl.class);

    @Autowired
    private OrderDao orderDao;

    /**
     * 判断客户名下是否有某个订单
     * @param orderId 订单号
     */
    @Override
    public boolean isOrderExist(Long orderId, Long customerId) {
        // 校验订单 id 是否存在 / 属于用户？
        long countRes = orderDao.countOrders(orderId, customerId, null, false);
        return countRes == 1;
    }
}
