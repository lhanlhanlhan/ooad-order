package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.service.CouponService;
import cn.edu.xmu.oomall.order.connector.service.CustomerService;
import cn.edu.xmu.oomall.order.connector.service.ShopService;
import cn.edu.xmu.oomall.order.dao.OrderDao;
import cn.edu.xmu.oomall.order.enums.OrderStatus;
import cn.edu.xmu.oomall.order.enums.OrderType;
import cn.edu.xmu.oomall.order.enums.ResponseCode;
import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.model.po.OrderEditPo;
import cn.edu.xmu.oomall.order.model.po.OrderItemPo;
import cn.edu.xmu.oomall.order.model.po.OrderPo;
import cn.edu.xmu.oomall.order.model.po.OrderSimplePo;
import cn.edu.xmu.oomall.order.model.vo.*;
import cn.edu.xmu.oomall.order.utils.APIReturnObject;
import cn.edu.xmu.oomall.order.utils.Accessories;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * @author Han Li
 * Created at 25/11/2020 8:53 上午
 * Modified by Han Li at 4/12/2020 2:38 下午
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

    @Autowired
    private CouponService couponService;

    @Autowired
    private FreightService freightService;

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
            APIReturnObject<PageInfo<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId, null, false);
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
            APIReturnObject<List<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId, null, false);
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
            return returnObject;
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
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于用户【404 返回】
            return returnObject;
        }

        // 检查是否修改过 [29/11/2020 - 邱明：这个规定取消]
        // 检查本来地址、新地址的地区一致性？是这么检查的马
        Long newRegionId = orderEditVo.getRegionId();
        Long oldRegionId = orderEditVo.getRegionId();
        if (newRegionId != null && oldRegionId != null && !newRegionId.equals(oldRegionId)) {
            // 地区码不一致
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_MODIFY_REGION_FORBIDDEN);
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.canModify()) {
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
            return returnObject;
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        boolean deletable = order.canDelete();
        boolean cancelable = order.canCustomerCancel();

        // 创造更改体
        OrderEditPo delPo;
        // 删除、取消只能二选一
        if (deletable) {
            delPo = new OrderEditPo();
            delPo.setBeDeleted(true);
        } else if (cancelable) {
            delPo = new OrderEditPo();
            delPo.setState(OrderStatus.CANCELLED.getCode());
            delPo.setSubState(null);
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
            return returnObject;
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.canSign()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 买家确认收货
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setState(OrderStatus.SIGNED.getCode());
        po.setSubState(null);

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
            return returnObject;
        }

        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.canCustomerChangeFromGrouponToNormal()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 更改订单类型为普通订单
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setOrderType(OrderType.NORMAL.getCode()); // 普通订单
        po.setState(OrderStatus.PAID.getCode()); // 状态改为已支付
        po.setSubState(null);

        // TODO - 还会不会触发更复杂的逻辑？

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
            APIReturnObject<PageInfo<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, page, pageSize, customerId, shopId, true);
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
            APIReturnObject<List<OrderSimplePo>> returnObject = orderDao.getSimpleOrders(orderSn, state, beginTime, endTime, customerId, shopId, true);
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

        // 数据库部分

        LocalDateTime nowTime = LocalDateTime.now();

        // 创建订单
        OrderPo orderPo = new OrderPo();
        orderPo.setCustomerId(orderVo.getCustomerId());
        orderPo.setRegionId(orderVo.getRegion_id());
        orderPo.setAddress(orderVo.getAddress());
        orderPo.setMobile(orderVo.getMobile());
        orderPo.setMessage(orderVo.getMessage());
        orderPo.setConsignee(orderVo.getConsignee());
        orderPo.setShopId(shopId);
        orderPo.setOrderSn(Accessories.genSerialNumber()); // 暂时用 UUID 生成 sn
        orderPo.setOriginPrice(0L); // 订单的各种价格都是 0
        orderPo.setDiscountPrice(0L);
        orderPo.setFreightPrice(0L);
        orderPo.setOrderType(OrderType.NORMAL.getCode()); // 订单种类为普通订单
        orderPo.setState(OrderStatus.AFTER_SALE_PENDING_SHIPMENT.getCode()); // 订单状态为已支付 (售后单待发货)
        orderPo.setGmtCreate(nowTime);

        // 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // 记录进订单系统
        List<OrderItemPo> orderItemPoList = new ArrayList<>(orderItemList.size());
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
            orderItemPo.setGmtCreate(nowTime);

            // 记录进订单系统
            if (!insertOrderItemPo(orderItemPo)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            orderItemPoList.add(orderItemPo);
        }

        // 获取订单完整资讯并返回
        return makeFullOrder(orderPo, orderItemPoList);
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
            return returnObject;
        }
        // 检查订单状态是否允许修改
        Order order = returnObject.getData();
        if (!order.canModify()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
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
            return returnObject;
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
            return returnObject;
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.canShopCancel()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }
        delPo = new OrderEditPo();
        delPo.setState(OrderStatus.CANCELLED.getCode());
        delPo.setSubState(null);
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
            return returnObject;
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        Order order = returnObject.getData();
        if (!order.canDeliver()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }
        delPo = new OrderEditPo();
        delPo.setShipmentSn(deliverSn);
        delPo.setState(OrderStatus.SHIPPED.getCode());
        delPo.setSubState(null);
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }

    /**
     * 创建普通订单
     * TODO - 秒杀订单的创建
     * @param newOrderVo 新订单申请
     * @return APIReturnObject<?>
     */
    @Transactional
    public APIReturnObject<?> createNormalOrder(Long customerId, NewOrderVo newOrderVo) {
        // TODO - 秒杀的认定

        // TODO - 优惠活动金额的计算
        List<OrderItemVo> orderItemVos = newOrderVo.getOrderItems();
        List<Map<String, Object>> orderItems = orderItemVos.stream()
                .map(OrderItemVo::toMap)
                .collect(Collectors.toList());
        int calcRet = couponService.computeDiscount(orderItems);
        if (calcRet != 0) {
            // TODO - 计算出错，返回对应错误
            return new APIReturnObject<>(ResponseCode.BAD_REQUEST);
        }

        // 计算运费
        Long regionId = newOrderVo.getRegionId();
        APIReturnObject<?> freightCalcRes = freightService.calcFreight(regionId, orderItemVos);
        if (freightCalcRes.getCode() != ResponseCode.OK) {
            return freightCalcRes;
        }
        Long totalFreight = (Long) freightCalcRes.getData();

        // 下单，扣库存
        for (Map<String, Object> itemInfo : orderItems) {
            if (!decreaseStock(itemInfo)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.GOODS_NOT_IN_STOCK);
            }
        }

        // TODO - 用优惠券计算优惠金额 我晕了
        long totalDiscount = 0L;

        // TODO - 核销优惠券

        /* 以下是数据库部分 */

        LocalDateTime nowTime = LocalDateTime.now();

        // 计算各商品的价格及其对应 Po
        List<OrderItemPo> orderItemPos = new ArrayList<>(orderItems.size());
        long totalPrice = 0L;
        for (Map<String, Object> item : orderItems) {
            Long skuId = (Long) item.get("skuId");
            Integer quantity = (Integer) item.get("quantity");
            // 创建新 po，设置除了 orderId、beSharedId 以外的资料
            OrderItemPo orderItemPo = new OrderItemPo();
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 联系商品模块获取商品资料
            Map<String, Object> skuInfo = shopService.getSkuInfo(skuId);
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 计算各种价格
            Long price = (Long) skuInfo.get("price");
            totalPrice += price * quantity;
            Long discount = (Long) item.get("discount");
            totalDiscount += discount;
            // 填写各种价格
            orderItemPo.setPrice(price);
            orderItemPo.setDiscount(discount);
            orderItemPo.setName((String) skuInfo.get("name"));
            orderItemPo.setGmtCreate(nowTime);
            // 放入容器
            orderItemPos.add(orderItemPo);
        }

        // 创建订单对应 Vo
        OrderPo orderPo = createNewOrderPo(customerId, newOrderVo);
        orderPo.setShopId(null); // TODO - 店铺 id 暂时为空，等支付后分单再说
        // 填入订单的各种价格
        orderPo.setOriginPrice(totalPrice);
        orderPo.setDiscountPrice(totalDiscount);
        orderPo.setFreightPrice(totalFreight);
        // 订单种类为普通订单，订单状态为待支付
        orderPo.setOrderType(OrderType.NORMAL.getCode());
        orderPo.setState(OrderStatus.PENDING_PAY.getCode()); // 普通订单没有 subState
        orderPo.setGmtCreate(nowTime);
        orderPo.setOrderSn(Accessories.genSerialNumber());

        // 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // TODO - 核销分享

        // 填入刚刚创建的订单的 id，放入所有 orderItemPo 中，并且写入数据库
        for (OrderItemPo itemPo : orderItemPos) {
            itemPo.setOrderId(orderId);
            itemPo.setBeShareId(null); // TODO - 分享 id？
            // 记录进订单系统
            if (!insertOrderItemPo(itemPo)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
        }

        // 获取订单完整资讯并返回
        return makeFullOrder(orderPo, orderItemPos);
    }

    /**
     * 创建单品订单 (团购/预售)
     * @param newOrderVo 新订单申请
     * @return APIReturnObject<?>
     */
    @Transactional
    public APIReturnObject<?> createOneItemOrder(Long customerId, NewOrderVo newOrderVo, OrderType type) {
        List<OrderItemVo> orderItemVos = newOrderVo.getOrderItems();
        Map<String, Object> itemInfo = orderItemVos.get(0).toMap();

        // 计算运费
        Long regionId = newOrderVo.getRegionId();
        APIReturnObject<?> freightCalcRes = freightService.calcFreight(regionId, orderItemVos);
        if (freightCalcRes.getCode() != ResponseCode.OK) {
            return freightCalcRes;
        }
        Long totalFreight = (Long) freightCalcRes.getData();

        // 扣库存
        if (!decreaseStock(itemInfo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.GOODS_NOT_IN_STOCK);
        }

        // TODO - 用团购/预售规则计算商品优惠
        long totalDiscount = 0L;

        /* 以下是数据库部分 */

        // 创建对应 Po
        Long skuId = (Long) itemInfo.get("skuId");
        Integer quantity = (Integer) itemInfo.get("quantity");
        // 联系商品模块获取商品资料
        Map<String, Object> skuInfo = shopService.getSkuInfo(skuId);
        Long price = (Long) skuInfo.get("price");

        LocalDateTime nowTime = LocalDateTime.now();

        // 创建新 OrderItemPo，设置除了 orderId、beSharedId 以外的资料
        OrderItemPo orderItemPo = new OrderItemPo();
        orderItemPo.setGoodsSkuId(skuId);
        orderItemPo.setQuantity(quantity);
        orderItemPo.setGoodsSkuId(skuId);
        orderItemPo.setQuantity(quantity);
        orderItemPo.setPrice(price * quantity);
        orderItemPo.setDiscount(totalDiscount);
        orderItemPo.setName((String) skuInfo.get("name"));
        orderItemPo.setGmtCreate(nowTime);

        // 创建订单对应 Po
        OrderPo orderPo = createNewOrderPo(customerId, newOrderVo);
        orderPo.setShopId((Long) skuInfo.get("shopId")); // 团购/预售的商铺号已知，因此订单的店铺 id 设为商品的店铺 Id
        orderPo.setOriginPrice(price);
        orderPo.setDiscountPrice(0L);
        orderPo.setFreightPrice(totalFreight);
        orderPo.setGmtCreate(nowTime);
        orderPo.setOrderSn(Accessories.genSerialNumber());
        orderPo.setOrderType(type.getCode()); // 订单种类为团购/预售订单，订单状态为待支付/待支付定金
        if (type == OrderType.PRE_SALE) {
            // 预售订单，待支付 + 待支付定金
            orderPo.setState(OrderStatus.PENDING_PAY.getCode());
            orderPo.setSubstate(OrderStatus.PENDING_DEPOSIT.getCode());
            orderPo.setPresaleId(newOrderVo.getPresaleId());
        } else {
            // 团购订单，待支付
            orderPo.setState(OrderStatus.PENDING_PAY.getCode());
            orderPo.setGrouponId(newOrderVo.getGrouponId());
        }

        // 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // TODO - 核销分享

        // 填入刚刚创建的订单的 id，放入所有 orderItemPo 中，并且写入数据库
        orderItemPo.setOrderId(orderId);
        orderItemPo.setBeShareId(null); // TODO - 分享 id？
        // 记录进订单项目系统
        if (!insertOrderItemPo(orderItemPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        // 获取订单完整资讯并返回
        List<OrderItemPo> orderItemPoList = new ArrayList<>(1);
        orderItemPoList.add(orderItemPo);
        return makeFullOrder(orderPo, orderItemPoList);
    }

    /**
     * **内部方法**：获取订单完整资讯并返回 (不找数据库)
     * @param orderPo 订单数据库对象
     * @param orderItemPoList 订单项目数据库对象列表
     * @return
     */
    private APIReturnObject<OrderVo> makeFullOrder(OrderPo orderPo, List<OrderItemPo> orderItemPoList) {
        // 把 orderItemPoList 塞入 orderPo 中
        orderPo.setOrderItemList(orderItemPoList);
        // 新建业务对象
        Order order = new Order(orderPo);
        OrderVo vo = order.createVo();
        // 补充 Vo 的 Customer 信息：联系其他模块下载
        Map<String, Object> customer = customerService.getCustomerInfo(order.getCustomerId());
        vo.setCustomer(customer);
        // 补充 Vo 的 Shop 信息 (如有)：联系其他模块下载
        Long shopId = order.getShopId();
        if (shopId != null) {
            // 注意到分单前的订单，商铺可能不止一家；这里只针对 shopId 不为空的情况 (团购/预售等商铺号已知的情况)
            Map<String, Object> shop = shopService.getShopInfo(shopId);
            vo.setShop(shop);
        }
        return new APIReturnObject<>(vo);
    }

    /**
     * **内部方法** 根据 Vo 新建 OrderPo
     * @param customerId
     * @param newOrderVo
     * @return
     */
    private OrderPo createNewOrderPo(Long customerId, NewOrderVo newOrderVo) {
        OrderPo orderPo = new OrderPo();
        orderPo.setCustomerId(customerId);
        orderPo.setRegionId(newOrderVo.getRegionId());
        orderPo.setAddress(newOrderVo.getAddress());
        orderPo.setMobile(newOrderVo.getMobile());
        orderPo.setMessage(newOrderVo.getMessage());
        orderPo.setConsignee(newOrderVo.getConsignee());
        return orderPo;
    }

    /**
     * **内部方法** 根据 OrderItemInfo 扣库存
     * // TODO - redis
     * @param itemInfo
     * @return
     */
    private boolean decreaseStock(Map<String, Object> itemInfo) {
        Long skuId = (Long) itemInfo.get("skuId");
        Integer quantity = (Integer) itemInfo.get("quantity");
        int decStatus = shopService.decreaseStock(skuId, quantity);
        if (decStatus == 1) {
            // 库存不足
            logger.warn("not in stock: skuid=" + skuId);
            return false;
        }
        return true;
    }

    /**
     * **内部方法**：将 orderItemPo 插入数据库中
     * @param itemPo
     * @return
     */
    private boolean insertOrderItemPo(OrderItemPo itemPo) {
        try {
            int response = orderDao.addOrderItem(itemPo);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * **内部方法**：将 orderItemPo 插入数据库中
     * @param orderPo
     * @return
     */
    private boolean insertOrderPo(OrderPo orderPo) {
        try {
            int response = orderDao.addOrder(orderPo);
            return response > 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
