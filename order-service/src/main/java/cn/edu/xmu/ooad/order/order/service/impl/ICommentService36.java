package cn.edu.xmu.ooad.order.order.service.impl;

import cn.edu.xmu.ooad.goods.require.ICommentService;
import cn.edu.xmu.ooad.goods.require.model.OrderSimple;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.ResponseCode;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@DubboService
@Component // 注册为 spring 的 bean 否则没法用 Autowired
public class ICommentService36 implements ICommentService {

    @Autowired
    private OrderDao orderDao;

    @Override
    public OrderSimple getComment(Long orderItemId) {
        // 拿 item
        OrderItemPo itemPo = orderDao.getOrderItemPo(orderItemId);
        if (itemPo == null) {
            return null;
        }
        // 拿 order
        APIReturnObject<Order> orderRetObj = orderDao.getSimpleOrder(itemPo.getOrderId(), null, null, false);
        if (orderRetObj.getCode() != ResponseCode.OK) {
            return null;
        }
        Order order = orderRetObj.getData();
        // 转对象
        return new OrderSimple(order.getCustomerId(), itemPo.getGoodsSkuId());
    }
}