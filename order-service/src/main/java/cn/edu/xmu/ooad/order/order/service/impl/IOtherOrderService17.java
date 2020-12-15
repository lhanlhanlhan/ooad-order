package cn.edu.xmu.ooad.order.order.service.impl;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.ResponseCode;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.other.required.IOtherOrderService;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemComplex;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemSimple;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSend;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSimple;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class IOtherOrderService17 implements IOtherOrderService {

    @Autowired
    private OrderDao orderDao;

    /**根据orderItemId获取orderItem相关信息，若资源不存在返回空
     * @author chenqw
     * @param orderItemId
     * @return OrderItemSimple or null
     */
    @Override
    public OtherOrderItemSimple getOrderItemByOrderItemId(Long orderItemId) {
        OrderItem orderItem = orderDao.getOrderItem(orderItemId);
        if (orderItem == null) {
            return null;
        }
        return getOtherOrderItemSimple(orderItem);
    }

    /**根据orderItemId获取orderItem相关信息和order信息，若资源不存在返回空
     *
     * @param orderItemId
     * @return
     */
    @Override
    public OtherOrderItemComplex getOrderItemComplexByOrderItemId(Long orderItemId) {
        OrderItem orderItem = orderDao.getOrderItem(orderItemId);
        if (orderItem == null) {
            return null;
        }
        // 查找 order
        APIReturnObject<Order> order = orderDao.getSimpleOrder(orderItem.getOrderId(), null, null, true);
        if (order.getData() == null) {
            return null;
        }
        OtherOrderItemComplex orderItemComplex = new OtherOrderItemComplex();
        orderItemComplex.setOrderItemId(orderItem.getId());
        orderItemComplex.setOrderId(orderItem.getOrderId());
        orderItemComplex.setPrice(orderItem.getPrice());
        orderItemComplex.setQuantity(orderItem.getQuantity());
        orderItemComplex.setSkuId(orderItem.getSkuId());
        orderItemComplex.setSkuName(orderItem.getName());

        Order trueOrder = order.getData();
        orderItemComplex.setOrderSn(trueOrder.getOrderSn());
        orderItemComplex.setShopId(trueOrder.getShopId());
        return orderItemComplex;
    }

    /**根据orderItemId获取全部orderItem信息和order信息，若资源不存在返回空
     * @author chenqw
     * @param orderItemId
     * @return
     */
    @Override
    public OtherOrderSimple getOrderByOrderItemId(Long orderItemId) {
        OrderItem orderItem = orderDao.getOrderItem(orderItemId);
        if (orderItem == null) {
            return null;
        }
        // 查找 order
        APIReturnObject<Order> orderObj = orderDao.getOrder(orderItem.getOrderId(), null, null, true);
        if (orderObj.getData() == null) {
            return null;
        }

        Order order = orderObj.getData();

        OtherOrderSimple orderSimple = new OtherOrderSimple();
        List<OtherOrderItemSimple> orderItemSimpleList = order.getOrderItemList()
                .stream()
                .map(this::getOtherOrderItemSimple)
                .collect(Collectors.toList());

        orderSimple.setOrderItems(orderItemSimpleList);
        orderSimple.setOrderId(orderItem.getOrderId());
        orderSimple.setOrderSn(order.getOrderSn());
        orderSimple.setShopId(order.getShopId());

        return orderSimple;
    }

    @Override
    public Long createASOrder(OtherOrderSend order) {
        return null;
    }

    // 相同部分
    private OtherOrderItemSimple getOtherOrderItemSimple(OrderItem item) {
        OtherOrderItemSimple orderItemSimple = new OtherOrderItemSimple();
        orderItemSimple.setOrderItemId(item.getId());
        orderItemSimple.setOrderId(item.getOrderId());
        orderItemSimple.setPrice(item.getPrice());
        orderItemSimple.setQuantity(item.getQuantity());
        orderItemSimple.setSkuId(item.getSkuId());
        orderItemSimple.setSkuName(item.getName());
        return orderItemSimple;
    }
}
