package cn.edu.xmu.ooad.order.order.service.impl;

import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.service.mqproducer.MQService;
import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.other.required.IOtherOrderService;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemComplex;
import cn.edu.xmu.ooad.other.required.model.OtherOrderItemSimple;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSend;
import cn.edu.xmu.ooad.other.required.model.OtherOrderSimple;
import cn.edu.xmu.ooad.util.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DubboService
@Component // 注册为 spring 的 bean 否则没法用 Autowired
public class IOtherOrderService17 implements IOtherOrderService {

    @Autowired
    private OrderDao orderDao;

    @DubboReference(check = false)
    private IShopService iShopService;

    /**
     * 根据orderItemId获取orderItem相关信息，若资源不存在返回空
     *
     * @param orderItemId
     * @return OrderItemSimple or null
     * @author chenqw
     */
    @Override
    public OtherOrderItemSimple getOrderItemByOrderItemId(Long orderItemId) {
        OrderItem orderItem = orderDao.getOrderItem(orderItemId);
        if (orderItem == null) {
            return null;
        }
        return getOtherOrderItemSimple(orderItem);
    }

    /**
     * 根据orderItemId获取orderItem相关信息和order信息，若资源不存在返回空
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
        Order order = orderDao.getSimpleOrder(orderItem.getOrderId(), true);
        if (order == null) {
            return null;
        }
        OtherOrderItemComplex orderItemComplex = new OtherOrderItemComplex();
        orderItemComplex.setOrderItemId(orderItem.getId());
        orderItemComplex.setOrderId(orderItem.getOrderId());
        orderItemComplex.setPrice(orderItem.getPrice());
        orderItemComplex.setQuantity(orderItem.getQuantity());
        orderItemComplex.setSkuId(orderItem.getSkuId());
        orderItemComplex.setSkuName(orderItem.getName());

        orderItemComplex.setOrderSn(order.getOrderSn());
        orderItemComplex.setShopId(order.getShopId());
        return orderItemComplex;
    }

    /**
     * 根据orderItemId获取全部orderItem信息和order信息，若资源不存在返回空
     *
     * @param orderItemId
     * @return
     * @author chenqw
     */
    @Override
    public OtherOrderSimple getOrderByOrderItemId(Long orderItemId) {
        OrderItem orderItem = orderDao.getOrderItem(orderItemId);
        if (orderItem == null) {
            return null;
        }
        // 查找 order
        Order order = orderDao.getOrder(orderItem.getOrderId(), true);
        if (order == null) {
            return null;
        }

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

    /**
     * 创建 售后 (AS) 订单
     * @param order
     * @return
     */
    @Transactional
    @Override
    public Long createASOrder(OtherOrderSend order) {
        // 创建售后订单
        LocalDateTime nowTime = Accessories.secondTime(LocalDateTime.now());

        // 创建订单
        OrderPo orderPo = new OrderPo();
        orderPo.setCustomerId(order.getCustomerId());
        orderPo.setRegionId(order.getRegionId());
        orderPo.setAddress(order.getAddress());
        orderPo.setMobile(order.getMobile());
        orderPo.setConsignee(order.getConsignee());
        orderPo.setShopId(order.getShopId());
        orderPo.setOrderSn(Accessories.genSerialNumber()); // 暂时用 UUID 生成 sn
        orderPo.setOriginPrice(0L); // 订单的各种价格都是 0
        orderPo.setDiscountPrice(0L);
        orderPo.setFreightPrice(0L);
        orderPo.setOrderType(OrderType.NORMAL.getCode()); // 订单种类为普通订单

        orderPo.setState(OrderStatus.PENDING_RECEIVE.getCode()); // 订单状态为已支付 (售后单待发货)
        orderPo.setSubstate(OrderChildStatus.PAID.getCode());
        orderPo.setGmtCreate(nowTime);
        orderPo.setGmtModified(nowTime);

        // MyBatis: 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("写入售后订单失败！");
            return null;
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // MyBatis: 填入刚刚创建的订单的 id，放入 orderItemPo 中，并且写入数据库
        OrderItemPo po = new OrderItemPo();
        SkuInfo skuInfo = iShopService.getSkuInfo(order.getSkuId());
        if (skuInfo == null) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("创建售后订单：查询 SKU 信息失败！skuId=" + order.getSkuId());
            return null;
        }
        po.setName(skuInfo.getName());
        po.setDiscount(0L);
        po.setGmtCreate(nowTime);
        po.setGmtModified(nowTime);
        po.setOrderId(orderId);
        po.setPrice(0L);
        po.setQuantity(order.getQuantity());

        // 记录进订单系统
        if (!insertOrderItemPo(po)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
        return orderPo.getId();
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

    /**
     * **内部方法**：将 orderItemPo 插入数据库中
     *
     * @param itemPo
     * @return
     */
    private boolean insertOrderItemPo(OrderItemPo itemPo) {
        try {
            int response = orderDao.addOrderItem(itemPo);
            return response > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 orderItemPo 插入数据库中
     *
     * @param orderPo
     * @return
     */
    private boolean insertOrderPo(OrderPo orderPo) {
        try {
            int response = orderDao.addOrder(orderPo);
            return response > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
