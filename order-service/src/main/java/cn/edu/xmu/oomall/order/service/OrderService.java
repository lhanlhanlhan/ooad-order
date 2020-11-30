package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.enums.OrderStatus;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.OrderEditPo;
import cn.edu.xmu.oomall.order.model.po.OrderItemPo;
import cn.edu.xmu.oomall.order.model.po.OrderPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * @author Han Li
 * Created at 25/11/2020 8:53 上午
 * Modified by Han Li at 25/11/2020 8:53 上午
 */
@Service
public class OrderService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShopService shopService;

    /**
     * 服务 o1：获取用户名下所有订单概要
     *
     * @param orderSn    订单号
     * @param state      状态码
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param page       页码
     * @param pageSize   页大小
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Han Li
     * Created at 25/11/2020 16:58
     * Created by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> getCustomerOrders(String orderSn, Byte state,
                                                String beginTime, String endTime,
                                                Integer page, Integer pageSize,
                                                Long customerId) {
        List<OrderSimpleVo> orders;
        Map<String, Object> returnObj = new HashMap<>();
        // 需要分页
        if (page != null && pageSize != null) {
            PageHelper.startPage(page, pageSize);
            // 调用 Dao 层
            APIReturnObject<PageInfo<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId, null);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            PageInfo<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.getList().stream()
                    .map(Order::new)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", orderSimplePos.getPageNum());
            returnObj.put("pageSize", orderSimplePos.getPageSize());
            returnObj.put("total", orderSimplePos.getTotal());
            returnObj.put("pages", orderSimplePos.getPages());
        }
        // 不必分页
        else {
            // 调用 Dao 层
            APIReturnObject<List<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId, null);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            List<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.stream()
                    .map(Order::new)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", 1);
            returnObj.put("pageSize", orders.size());
            returnObj.put("total", orders.size());
            returnObj.put("pages", 1);
        }
        // 返回【标准返回】
        returnObj.put("list", orders);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 o2：获取用户名下订单完整信息
     *
     * @author Han Li
     * Created at 26/11/2020 11:15
     * Created by Han Li at 26/11/2020 11:15
     * @param id 订单 id
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public APIReturnObject<?> getOrder(Long id, Long customerId) {
        // 获取订单 Bo、Vo
        APIReturnObject<Order> returnObject = orderDao.getOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        Order order = returnObject.getData();
        OrderVo vo = order.createVo();

        // 补充 Vo 的 Customer 信息：联系其他模块
        Map<String, Object> customer = customerService.getCustomerInfo(id);
        vo.setCustomer(customer);
        // 补充 Vo 的 Shop 信息：联系商品模块
        Long shopId = order.getShopId();
        Map<String, Object> shop = shopService.getShopInfo(shopId);
        vo.setShop(shop);

        // 封装并返回【标准返回】
        return new APIReturnObject<>(vo);
    }


    /**
     * 服务 o3：买家修改订单信息
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @param orderEditVo 修改信息对象
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerModifyOrder(Long id, Long customerId, OrderEditVo orderEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, true);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // TODO - 检查是否修改过
        // TODO - 检查本来地址、新地址的地区一致性

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.isCustomerModifiable()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 自定义修改字段
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setAddress(orderEditVo.getAddress());
        po.setConsignee(orderEditVo.getConsignee());
        po.setMobile(orderEditVo.getMobile());
        po.setRegionId(orderEditVo.getRegionId());

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o4：买家删掉 / 取消订单订单
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerDelOrCancelOrder(Long id, Long customerId) {
        // 查询订单，检查所有者
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        boolean deletable = order.isDeletable();
        boolean cancelable = order.isCancelable();

        // 创造更改体
        OrderEditPo delPo;
        // 删除、取消只能二选一
        if (deletable) {
            delPo = new OrderEditPo();
            delPo.setBeDeleted(true);
        } else if (cancelable) {
            delPo = new OrderEditPo();
            delPo.setState(OrderStatus.CANCELLED.getCode());
        } else {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }

    /**
     * 服务 o5：买家确认收货
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerConfirm(Long id, Long customerId) {
        // 查询订单，检查所有者
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.isCustomerCanSign()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 买家确认收货
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setState(OrderStatus.SIGNED.getCode());

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o6：买家将团购订单转为普通订单
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @param id 订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerChangeGroupon(Long id, Long customerId) {
        // 查询订单，检查所有者
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.isCustomerCanChangeToNormalOrder()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 更改订单类型为普通订单
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setOrderType((byte) 0); // 普通订单：0
        po.setState(OrderStatus.PAID.getCode()); // 状态改为已支付

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o7：获取商铺下所有订单概要
     *
     * @param orderSn    订单号
     * @param state      状态码
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param page       页码
     * @param pageSize   页大小
     * @param customerId 用户 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject
     * @author Han Li
     * Created at 25/11/2020 16:58
     * Created by Han Li at 25/11/2020 16:58
     */
    public APIReturnObject<?> getShopOrders(Long shopId, Long customerId,
                                            String orderSn, Byte state,
                                            String beginTime, String endTime,
                                            Integer page, Integer pageSize) {
        List<OrderSimpleVo> orders;
        Map<String, Object> returnObj = new HashMap<>();
        // 需要分页
        if (page != null && pageSize != null) {
            PageHelper.startPage(page, pageSize);
            // 调用 Dao 层
            APIReturnObject<PageInfo<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId, shopId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            PageInfo<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.getList().stream()
                    .map(Order::new)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", orderSimplePos.getPageNum());
            returnObj.put("pageSize", orderSimplePos.getPageSize());
            returnObj.put("total", orderSimplePos.getTotal());
            returnObj.put("pages", orderSimplePos.getPages());
        }
        // 不必分页
        else {
            // 调用 Dao 层
            APIReturnObject<List<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId, shopId);
            if (returnObject.getCode() != ResponseCode.OK) {
                return returnObject;
            }
            List<OrderSimplePo> orderSimplePos = returnObject.getData();
            // 转为业务对象列表
            orders = orderSimplePos.stream()
                    .map(Order::new)
                    .map(Order::createSimpleVo)
                    .collect(Collectors.toList());
            // 用 Map 封装
            returnObj.put("page", 1);
            returnObj.put("pageSize", orders.size());
            returnObj.put("total", orders.size());
            returnObj.put("pages", 1);
        }
        // 返回【标准返回】
        returnObj.put("list", orders);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 o8：创建售后订单 (价格为 $0.- 的订单)
     * @param shopId 店铺 id
     * @param orderVo 订单详细资料
     * @return APIReturnObject
     */
    @Transactional // TODO - 扣库存如何回滚？？
    public APIReturnObject<?> createAfterSaleOrder(Long shopId,
                                                   AfterSaleOrderVo orderVo) {
        // 扣库存
        List<Map<String, Object>> orderItemList = orderVo.getOrderItems();
        for (Map<String, Object> item : orderItemList) {
            Long skuId = ((Integer) item.get("skuId")).longValue();
            Integer quantity = (Integer) item.get("quantity");
            // 联系商品模块扣库存
            int decreaseStatus = shopService.decreaseStock(skuId, quantity);
            if (decreaseStatus == 1) {
                logger.warn("not in stock: skuid=" + skuId);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.GOODS_NOT_IN_STOCK);
            }
        }

        // 创建订单
        OrderPo orderPo = new OrderPo();
        orderPo.setCustomerId(orderVo.getCustomerId());
        orderPo.setRegionId(orderVo.getRegion_id());
        orderPo.setAddress(orderVo.getAddress());
        orderPo.setMobile(orderVo.getMobile());
        orderPo.setMessage(orderVo.getMessage());
        orderPo.setConsignee(orderVo.getConsignee());
        orderPo.setShopId(shopId);
        // 订单的各种价格都是 0
        orderPo.setOriginPrice(0L);
        orderPo.setDiscountPrice(0L);
        orderPo.setFreightPrice(0L);
        // 订单种类为普通订单，订单状态为已支付 (售后单待发货)
        orderPo.setOrderType((byte) 0);
        orderPo.setState(OrderStatus.AFTER_SALE_PENDING_SHIPMENT.getCode());

        // 写入订单系统
        try {
            int response = orderDao.addOrder(orderPo);
            if (response <= 0) {
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // 记录进订单系统
        for (Map<String, Object> item : orderItemList) {
            Long skuId = ((Integer) item.get("skuId")).longValue();
            Integer quantity = (Integer) item.get("quantity");
            // 创建新 po
            OrderItemPo orderItemPo = new OrderItemPo();
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 联系商品模块获取商品资料
            Map<String, Object> skuInfo = shopService.getSkuInfo(skuId);
            orderItemPo.setOrderId(orderId);
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 这是售后订单，价格一律为 0，没有优惠券、没有优惠活动、没有推广
            orderItemPo.setPrice(0L);
            orderItemPo.setDiscount(0L);
            orderItemPo.setName((String) skuInfo.get("name"));
            orderItemPo.setGmtCreate(LocalDateTime.now());

            // 记录进订单系统
            try {
                int response = orderDao.addOrderItem(orderItemPo);
                if (response <= 0) {
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        }

        // 获取订单完整资讯并返回
        APIReturnObject<Order> returnObject = orderDao.getOrder(orderId, orderVo.getCustomerId(), null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        Order order = returnObject.getData();
        OrderVo vo = order.createVo();
        // 补充 Vo 的 Customer 信息：联系其他模块下载
        Map<String, Object> customer = customerService.getCustomerInfo(order.getCustomerId());
        vo.setCustomer(customer);
        // 补充 Vo 的 Shop 信息：联系其他模块下载
        Map<String, Object> shop = shopService.getShopInfo(order.getShopId());
        vo.setShop(shop);
        return new APIReturnObject<>(vo);
    }

    /**
     * 服务 o9：店家修改订单信息 [DONE]
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> shopModifyOrder(Long id, Long shopId, OrderEditVo orderEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, null, shopId, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于店铺【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        // 自定义修改字段
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setMessage(orderEditVo.getMessage());

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o10：获取店铺名下订单完整信息
     *
     * @author Han Li
     * Created at 26/11/2020 11:15
     * Created by Han Li at 26/11/2020 11:15
     * @param id 订单 id
     * @param shopId 店铺 id
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public APIReturnObject<?> getShopOrder(Long id, Long shopId) {
        // 获取订单 Bo、Vo
        APIReturnObject<Order> returnObject = orderDao.getOrder(id, null, shopId, true);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于店铺【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }
        Order order = returnObject.getData();
        OrderVo vo = order.createVo();

        // 补充 Vo 的 Customer 信息：联系其他模块
        Map<String, Object> customer = customerService.getCustomerInfo(id);
        vo.setCustomer(customer);
        // 补充 Vo 的 Shop 信息：联系商品模块
        Map<String, Object> shop = shopService.getShopInfo(shopId);
        vo.setShop(shop);

        // 封装并返回【标准返回】
        return new APIReturnObject<>(vo);
    }

    /**
     * 服务 o11：店铺取消订单订单
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> shopCancelOrder(Long id, Long shopId) {
        // 查询订单，检查所有者
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, null, shopId, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于 shop【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.isShopCancelable()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }
        delPo = new OrderEditPo();
        delPo.setState(OrderStatus.CANCELLED.getCode());
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }

    /**
     * 服务 o12：店铺发货
     *
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     * @return cn.edu.xmu.oomall.order.utils.APIReturnObject<?>
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> shopDeliverOrder(Long id, Long shopId, String deliverSn) {
        // 查询订单，检查所有者
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, null, shopId, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于 shop【404 返回】
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, returnObject.getCode(), returnObject.getErrMsg());
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.isShopCanDeliver()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }
        delPo = new OrderEditPo();
        delPo.setShipmentSn(deliverSn);
        delPo.setState(OrderStatus.SHIPPED.getCode());
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }
}
