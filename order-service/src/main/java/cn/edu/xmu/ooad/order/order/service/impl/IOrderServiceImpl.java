package cn.edu.xmu.ooad.order.order.service.impl;

import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 供其他两个模块调用的 订单服务 实现类
 *
 * @author Han Li
 * Created at 8/12/2020 10:14 上午
 * Modified by Han Li at 8/12/2020 10:14 上午
 */
//@Service(version = "${dubbo.application.version}")
public class IOrderServiceImpl {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(IOrderServiceImpl.class);

    @Autowired
    private OrderDao orderDao;

}
