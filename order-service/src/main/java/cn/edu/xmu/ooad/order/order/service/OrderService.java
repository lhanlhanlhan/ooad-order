package cn.edu.xmu.ooad.order.order.service;

import cn.edu.xmu.ooad.order.centre.annotations.RedisOptimized;
import cn.edu.xmu.ooad.order.centre.interfaces.IFreightServiceInside;
import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.RedisUtils;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.bo.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.GrouponOrder;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.po.OrderEditPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.order.model.vo.*;
import cn.edu.xmu.ooad.order.order.service.mqlistener.model.CreateOrderDemand;
import cn.edu.xmu.ooad.order.require.*;
import cn.edu.xmu.ooad.order.require.models.*;
import cn.edu.xmu.ooad.order.order.service.mqproducer.MQService;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.centre.utils.ResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

    @DubboReference(check = false)
    private IShopService iShopService;

    @DubboReference(check = false)
    private ICustomerService iCustomerService;

    @DubboReference(check = false)
    private ICouponService iCouponService;

    @DubboReference(check = false)
    private IFreightServiceInside iFreightServiceInside;

    @Autowired
    private MQService mqService;

    @Autowired
    private RedisUtils redisUtils;

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
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
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
                    .map(OrderSimpleVo::new)
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
                    .map(OrderSimpleVo::new)
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
     * @param id         订单 id
     * @param customerId 用户 id
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<java.util.Map < java.lang.String, java.lang.Object>>
     * @author Han Li
     * Created at 26/11/2020 11:15
     * Created by Han Li at 26/11/2020 11:15
     */
    public APIReturnObject<?> getOrder(Long id, Long customerId) {
        // 獲取所請求之訂單的 Bo、Vo
        APIReturnObject<Order> returnObject = orderDao.getOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 捕獲到錯誤
            return returnObject;
        }
        Order order = returnObject.getData();
        OrderVo vo = new OrderVo(order);

        // 补充 Vo 的 Customer 信息：联系其他模块
        CustomerInfo customer = iCustomerService.getCustomerInfo(id);
        vo.setCustomer(customer);

        // 补充 Vo 的 Shop 信息：联系商品模块
        Long shopId = order.getShopId();
        ShopInfo shop = iShopService.getShopInfo(shopId);
        vo.setShop(shop);

        // 封装并返回【标准返回】
        return new APIReturnObject<>(vo);
    }


    /**
     * 服务 o3：买家修改订单信息
     *
     * @param id          订单号
     * @param customerId  消费者号
     * @param orderEditVo 修改信息对象
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerModifyOrder(Long id, Long customerId, OrderEditVo orderEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        APIReturnObject<Order> returnObject = orderDao.getSimpleOrder(id, customerId, null, false);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 捕獲到錯誤
            return returnObject;
        }

        // 检查是否修改过 [29/11/2020 - 邱明：这个规定取消]
        // TODO - 检查本来地址、新地址的地区一致性？是这么检查的马
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
     * @param id         订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
            delPo.setSubState((byte) -1); // sub state 置为空
            // 触发取消订单 (退款)
            if (order.triggerCancelled() != 0) {
                logger.error("订单取消失败, orderId=" + order.getId());
            }
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
     * @param id         订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
        po.setState(OrderStatus.DONE.getCode());
        po.setSubState((byte) -1); // sub state 置为空

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o6：买家将团购订单转为普通订单
     *
     * @param id         订单号
     * @param customerId 消费者号
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
        if (!(order instanceof GrouponOrder)) {
            // 订单种类不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_TYPE_NOT_CORRESPOND);
        }
        if (!((GrouponOrder) order).canChangeToNormal()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATE_NOT_ALLOW);
        }

        // 更改订单类型为普通订单
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setOrderType(OrderType.NORMAL.getCode()); // 普通订单
        po.setState(OrderStatus.PENDING_RECEIVE.getCode()); // 状态改为待发货
        po.setSubState(OrderChildStatus.PAID.getCode()); // 状态改为已支付

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
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject
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
                    .map(OrderSimpleVo::new)
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
                    .map(OrderSimpleVo::new)
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

//    /**
//     * 服务 o8：创建售后订单 (价格为 $0.- 的订单)
//     *
//     * @param shopId  店铺 id
//     * @param orderVo 订单详细资料
//     * @return APIReturnObject
//     */
//    @Transactional
//    public APIReturnObject<?> createAfterSaleOrder(Long shopId,
//                                                   AfterSaleOrderVo orderVo) {
//        // 扣库存
//        List<Map<String, Object>> orderItemList = orderVo.getOrderItems();
//        for (Map<String, Object> item : orderItemList) {
//            Long skuId = ((Integer) item.get("skuId")).longValue();
//            Integer quantity = (Integer) item.get("quantity");
//            // 联系商品模块扣库存
//            int decreaseStatus = shopService.decreaseStock(skuId, quantity);
//            if (decreaseStatus == 1) {
//                logger.warn("not in stock: skuid=" + skuId);
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.GOODS_NOT_IN_STOCK);
//            }
//        }
//
//        // 数据库部分
//
//        LocalDateTime nowTime = LocalDateTime.now();
//
//        // 创建订单
//        OrderPo orderPo = new OrderPo();
//        orderPo.setCustomerId(orderVo.getCustomerId());
//        orderPo.setRegionId(orderVo.getRegion_id());
//        orderPo.setAddress(orderVo.getAddress());
//        orderPo.setMobile(orderVo.getMobile());
//        orderPo.setMessage(orderVo.getMessage());
//        orderPo.setConsignee(orderVo.getConsignee());
//        orderPo.setShopId(shopId);
//        orderPo.setOrderSn(Accessories.genSerialNumber()); // 暂时用 UUID 生成 sn
//        orderPo.setOriginPrice(0L); // 订单的各种价格都是 0
//        orderPo.setDiscountPrice(0L);
//        orderPo.setFreightPrice(0L);
//        orderPo.setOrderType(OrderType.NORMAL.getCode()); // 订单种类为普通订单
//        orderPo.setState(OrderStatus.AFTER_SALE_PENDING_SHIPMENT.getCode()); // 订单状态为已支付 (售后单待发货)
//        orderPo.setGmtCreate(nowTime);
//
//        // 写入订单系统
//        if (!insertOrderPo(orderPo)) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
//        }
//
//        // 获取刚刚创建订单的 id
//        Long orderId = orderPo.getId();
//
//        // 记录进订单系统
//        List<OrderItemPo> orderItemPoList = new ArrayList<>(orderItemList.size());
//        for (Map<String, Object> item : orderItemList) {
//            Long skuId = ((Integer) item.get("skuId")).longValue();
//            Integer quantity = (Integer) item.get("quantity");
//            // 创建新 po
//            OrderItemPo orderItemPo = new OrderItemPo();
//            orderItemPo.setGoodsSkuId(skuId);
//            orderItemPo.setQuantity(quantity);
//            // 联系商品模块获取商品资料
//            SkuInfo skuInfo = shopService.getSkuInfo(skuId);
//            orderItemPo.setOrderId(orderId);
//            orderItemPo.setGoodsSkuId(skuId);
//            orderItemPo.setQuantity(quantity);
//            // 这是售后订单，价格一律为 0，没有优惠券、没有优惠活动、没有推广
//            orderItemPo.setPrice(0L);
//            orderItemPo.setDiscount(0L);
//            orderItemPo.setName(skuInfo.getName());
//            orderItemPo.setGmtCreate(nowTime);
//
//            // 记录进订单系统
//            if (!insertOrderItemPo(orderItemPo)) {
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
//            }
//            orderItemPoList.add(orderItemPo);
//        }
//
//        // 获取订单完整资讯并返回
//        return makeFullOrder(orderPo, orderItemPoList);
//    }

    /**
     * 服务 o9：店家修改订单信息 [DONE]
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
     * @param id     订单 id
     * @param shopId 店铺 id
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<java.util.Map < java.lang.String, java.lang.Object>>
     * @author Han Li
     * Created at 26/11/2020 11:15
     * Created by Han Li at 26/11/2020 11:15
     */
    public APIReturnObject<?> getShopOrder(Long id, Long shopId) {
        // 获取订单 Bo、Vo
        APIReturnObject<Order> returnObject = orderDao.getOrder(id, null, shopId, true);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 不存在、已删除、不属于店铺【404 返回】
            return returnObject;
        }
        Order order = returnObject.getData();
        OrderVo vo = new OrderVo(order);

        // 补充 Vo 的 Customer 信息：联系其他模块
        CustomerInfo customer = iCustomerService.getCustomerInfo(id);
        vo.setCustomer(customer);

        // 补充 Vo 的 Shop 信息：联系商品模块
        ShopInfo shop = iShopService.getShopInfo(shopId);
        vo.setShop(shop);

        // 封装并返回【标准返回】
        return new APIReturnObject<>(vo);
    }

    /**
     * 服务 o11：店铺取消订单订单
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
        delPo.setSubState((byte) -1); // sub state 置为空
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }

    /**
     * 服务 o12：店铺发货
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
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
        delPo.setState(OrderStatus.PENDING_RECEIVE.getCode());
        delPo.setSubState(OrderChildStatus.SHIPPED.getCode());
        delPo.setId(id);

        return orderDao.modifyOrder(delPo);
    }

    /**
     * 大服务 买家创建订单
     *
     * @param customerId
     * @param orderNewVo
     * @return
     */
    @RedisOptimized
    public APIReturnObject<?> createNormalOrder(Long customerId, OrderNewVo orderNewVo) {
        // 生成 SN
        String sn = Accessories.genSerialNumber();

        // 下单此刻时间
        LocalDateTime nowTime = LocalDateTime.now();

        // 在运算运费前，要提前获得商品模块 sku 信息，否则重复获取 sku 信息
        Map<Long, SkuInfo> skuInfoMap = new HashMap<>(orderNewVo.getOrderItems().size());
        for (OrderItemVo orderItemVo : orderNewVo.getOrderItems()) {
            SkuInfo skuInfo = iShopService.getSkuInfo(orderItemVo.getSkuId());
            if (skuInfo == null) {
                logger.debug("查无此商品, skuId=" + orderItemVo.getSkuId()); // TODO - 数据库穿透？
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST, "商品不存在");
            }
            skuInfoMap.put(orderItemVo.getSkuId(), skuInfo);
        }

        // 订单项转为业务对象
        List<OrderItem> orderItems = orderNewVo.getOrderItems()
                .stream()
                .map(OrderItem::new) // 这个 orderItem 没有 品名、没有价格等一系列需要 skuInfo 的资料、没有 orderId
                .collect(Collectors.toList());

        // 商品模块获得优惠卷信息
        final CouponInfo couponInfo;
        CouponInfo couponInfoDefer;
        final CopyOnWriteArrayList<OrderItem> couponSuitableOrderItems;
        if (orderNewVo.getCouponId() != null) {
            // 查询用户有无优惠券，如有，就查询该优惠券
            couponInfoDefer = iCouponService.getCoupon(customerId, orderNewVo.getCouponId());
            // 看看优惠券有无被使用及过期
            if (couponInfoDefer == null) { // 无此优惠券
                orderNewVo.setCouponId(null);
            } else { // 有优惠券，判断有无被使用 / 已失效及有无过期
                if (couponInfoDefer.getState() == 2 || couponInfoDefer.getState() == 3) { // 已使用 / 已失效
                    orderNewVo.setCouponId(null);
                    couponInfoDefer = null;
                } else if (nowTime.isBefore(couponInfoDefer.getBeginTime()) ||
                        nowTime.isAfter(couponInfoDefer.getEndTime())) { // 已过期
                    orderNewVo.setCouponId(null);
                    couponInfoDefer = null;
                }
            }
            // 其他情况，有优惠券、未使用、未过期
            couponSuitableOrderItems = new CopyOnWriteArrayList<>();
        } else {
            couponInfoDefer = null;
            couponSuitableOrderItems = null;
        }
        couponInfo = couponInfoDefer;

        // 从商品模块获取所有 item 的价格、名字、商店和所需求的优惠活动信息
        ConcurrentMap<Long, CouponActivityInfo> allCouponActs = new ConcurrentHashMap<>();
        orderItems.stream().forEach(orderItem -> {
            // 商品模块获取 SkuInfo
            SkuInfo skuInfo = iShopService.getSkuInfo(orderItem.getSkuId());
            orderItem.setName(skuInfo.getName());
            orderItem.setPrice(skuInfo.getPrice());
            orderItem.setDiscount(0L); // 为计算优惠作准备
            orderItem.setShopId(skuInfo.getShopId()); // 为计算优惠作准备
            // 检查 OrderItem 是否可用于 优惠券 的优惠活动
            if (couponInfo != null) {
                if (iCouponService.isSkuSuitsCouponActivity(orderItem.getSkuId(), couponInfo.getId())) {
                    // SKU 可用该优惠卷，于是添加到计算适用该优惠卷的优惠金额的 list 当中去
                    couponSuitableOrderItems.add(orderItem);
                }
            }
            // 商品模块获取 CouponActivity (如有) // 必须放在最后面
            Long couponActId = orderItem.getCouponActId();
            if (couponActId != null) {
                if (allCouponActs.get(couponActId) == null) {
                    // 查询商品模块
                    CouponActivityInfo cai = iCouponService.getCouponActivity(couponActId);
                    if (cai == null) { // 没有该活动，没必要放入 CouponActId
                        orderItem.setCouponActId(null);
                        return;
                    }
                    // 看看活动是否过期、是否开始
                    if (nowTime.isBefore(cai.getBeginTime()) || nowTime.isAfter(cai.getEndTime())) {
                        orderItem.setCouponActId(null);
                        return;
                    }
                    // 将活动放入总活动列表
                    allCouponActs.put(couponActId, cai);
                } // 如果已经放过活动就没必要查找、放入了
            }
        });

        // 重建优惠活动 (如有)，并计算优惠活动使用的商品的优惠金额
        if (allCouponActs.size() != 0) {
            for (CouponActivityInfo cai : allCouponActs.values()) {
                // 重建优惠规则
                BaseCouponDiscount actDisc;
                try {
                    actDisc = BaseCouponDiscount.getInstance(cai.getStrategy());
                } catch (JsonProcessingException e) { // 优惠规则不合法
                    e.printStackTrace();
                    logger.error("优惠活动优惠规则不合法！couponActId=" + cai.getId());
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("优惠活动优惠规则创建失败！couponActId=" + cai.getId() + " exception=" + e.getMessage());
                    return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                }
                // 获取适用于该优惠活动的商品
                List<OrderItem> couponActSuitableOrderItems = new LinkedList<>();
                for (OrderItem oi : orderItems) {
                    if (iCouponService.isSkuSuitsCouponActivity(oi.getSkuId(), cai.getId())) {
                        couponActSuitableOrderItems.add(oi);
                    }
                }
                // 计算该优惠活动之优惠金额
                actDisc.calcAndSetDiscount(couponActSuitableOrderItems);
            }
        }

        // 重建优惠券，并计算优惠券适用商品的优惠金额
        if (couponSuitableOrderItems != null &&
                couponSuitableOrderItems.size() != 0) {
            BaseCouponDiscount couponDiscount;
            // 重建优惠券
            try {
                couponDiscount = BaseCouponDiscount.getInstance(couponInfo.getCouponActivity().getStrategy());
            } catch (JsonProcessingException e) { // 优惠规则不合法
                e.printStackTrace();
                logger.error("优惠券优惠规则不合法！couponId=" + couponInfo.getId());
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("优惠券优惠规则创建失败！couponId=" + couponInfo.getId() + " exception=" + e.getMessage());
                return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
            }
            // 计算优惠金额
            couponDiscount.calcAndSetDiscount(couponSuitableOrderItems);
        }

        // 计算运费
        Long regionId = orderNewVo.getRegionId();
        long totalFreight = iFreightServiceInside.calcFreight(regionId, orderItems
                .stream()
                .map(OrderItem::toCalcItem)
                .collect(Collectors.toList()), skuInfoMap);
        if (totalFreight < 0) {
            logger.error("运费无法计算，orderSn=" + sn);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }

        /* *** 库存部分 *** */

        // Redis: 下单，扣库存、累加计算各种价格
        LinkedList<Long> writeBackQueue = new LinkedList<>(); // 写回队列，仅当事物成功运行才写回库存
        List<OrderItemPo> orderItemPos = new ArrayList<>(orderItems.size()); // 转换为 po
        Map<Long, Integer> succeededItemStocks = new HashMap<>(orderItems.size()); // 已成功扣 redis 库存的商品
        long totalPrice = 0L, totalDiscount = 0L;
        int decStockRes;
        for (OrderItem item : orderItems) {
            // 查看是否是秒杀商品
            String fsKey = "fs_" + item.getSkuId();
            Integer fsStock = redisUtils.get(fsKey, Integer.class);
            // ****** 秒杀商品处理 ******
            if (fsStock != null) {
                // 秒杀库存减
                decStockRes = decreaseFlashSaleStock(item.getSkuId(), item.getQuantity());
                if (decStockRes != 0) {
                    // 下单失败，充正 redis 库存变更
                    addBack(succeededItemStocks);
                    if (decStockRes == 1) {
                        // 库存不足
                        logger.debug("库存不足,skuId=" + item.getSkuId());
                        return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOT_ENOUGH, "库存不足");
                    } else {
                        return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                    }
                }
                // 计算、累加各种价格
                totalPrice += item.getPrice() * item.getQuantity();
                totalDiscount += item.getDiscount();
                // 放入 PO 容器 及 不放入充正列表
                orderItemPos.add(item.toPo());
            }
            // ****** 非秒杀商品处理 ******
            else {
                decStockRes = decreaseStock(item.getSkuId(), item.getQuantity(), writeBackQueue);
                if (decStockRes != 0) {
                    // 下单失败，充正 redis 库存变更
                    addBack(succeededItemStocks);
                    if (decStockRes == 1) {
                        logger.debug("库存不足,skuId=" + item.getSkuId());
                        return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOT_ENOUGH, "库存不足");
                    } else {
                        return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
                    }
                }
                // 计算、累加各种价格
                totalPrice += item.getPrice() * item.getQuantity();
                totalDiscount += item.getDiscount();
                // 放入 PO 容器及放入充正列表
                orderItemPos.add(item.toPo());
                succeededItemStocks.put(item.getSkuId(), item.getQuantity());
            }
        }

        // MyBatis: 创建订单对应 Po
        OrderPo orderPo = createNewOrderPo(customerId, orderNewVo); {
            orderPo.setShopId(null); // 店铺 id 暂时为空，等支付后分单再说
            // 填入订单的各种价格
            orderPo.setOriginPrice(totalPrice);
            orderPo.setDiscountPrice(totalDiscount);
            orderPo.setFreightPrice(totalFreight);
            // 订单种类为普通订单，订单状态为待支付
            orderPo.setOrderType(OrderType.NORMAL.getCode());
            orderPo.setGmtCreate(nowTime);
            orderPo.setOrderSn(sn);
        }


        // 提交新建订单的讯息
        CreateOrderDemand demand = new CreateOrderDemand();
        demand.setCId(customerId);
        demand.setO(orderPo);
        demand.setI(orderItemPos);
        demand.setType((byte) 0); // 普通订单
        if (couponInfo != null) {
            demand.setCouponId(couponInfo.getId());
        }
        demand.setWriteBackQueue(writeBackQueue);

        mqService.sendCreateOrderInfo(demand);
        return new APIReturnObject<>(sn);
    }


    @DubboReference(check = false)
    private IGrouponService iGrouponService;
    @DubboReference(check = false)
    private IPreSaleService iPreSaleService;


    @Transactional
    public APIReturnObject<?> createOneItemOrder(Long customerId, OrderNewVo orderNewVo, OrderType type) {
        // 生成 SN
        String sn = Accessories.genSerialNumber();

        // 下单此刻时间
        LocalDateTime nowTime = LocalDateTime.now();

        // 订单项转为业务对象
        OrderItem theItem = new OrderItem(orderNewVo.getOrderItems().get(0));
        SkuInfo skuInfo = iShopService.getSkuInfo(theItem.getSkuId());
        if (skuInfo == null) {
            logger.debug("查无此商品, skuId=" + theItem.getSkuId()); // TODO - 数据库穿透？
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_EXIST, "商品不存在");
        }

        // 查询团购、预售活动是否有效
        if (type == OrderType.GROUPON) {
            // 查询团购活动
            GrouponActivityInfo gai = iGrouponService.getSkuGrouponActivity(theItem.getSkuId());
            if (gai == null || !gai.getId().equals(orderNewVo.getGrouponId())) {
                // 团购活动无效
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.RESOURCE_NOT_EXIST, "团购活动无效");
            }
        } else {
            // 查询预售活动
            PreSaleActivityInfo psai = iPreSaleService.getSkuPreSaleActivity(theItem.getSkuId());
            if (psai == null || !psai.getId().equals(orderNewVo.getGrouponId())) {
                // 预售活动无效
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.RESOURCE_NOT_EXIST, "预售活动无效");
            }
        }

        Map<Long, SkuInfo> skuInfoMap = new HashMap<>(orderNewVo.getOrderItems().size());
        skuInfoMap.put(skuInfo.getId(), skuInfo);

        // 计算运费
        Long regionId = orderNewVo.getRegionId();
        List<FreightCalcItem> freightItemVo = new ArrayList<>(1);
        freightItemVo.add(theItem.toCalcItem());
        long totalFreight = iFreightServiceInside.calcFreight(regionId, freightItemVo, skuInfoMap);
        if (totalFreight < 0) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR, "计算运费失败");
        }

        /* 以下是数据库部分 */

        // Redis: 下单，扣库存、累加计算各种价格
        int decStockRes;
        decStockRes = decreaseStock(theItem.getSkuId(), theItem.getQuantity());
        if (decStockRes != 0) {
            // 下单失败
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOT_ENOUGH);
        }
        long totalPrice = theItem.getPrice() * theItem.getQuantity();

        // MyBatis: 创建订单对应 Po
        OrderPo orderPo = createNewOrderPo(customerId, orderNewVo);
        {
            orderPo.setShopId(skuInfo.getShopId()); // 店铺 id 设置为单品店铺 id
            // 填入订单的各种价格
            orderPo.setOriginPrice(totalPrice);
            orderPo.setDiscountPrice(0L); // 团购/预售没有优惠
            orderPo.setFreightPrice(totalFreight);
            // 种类及创建时间
            orderPo.setOrderType(type.getCode());
            orderPo.setGmtCreate(nowTime);
            orderPo.setOrderSn(sn);
        }

        return new APIReturnObject<>(sn);
    }



    /**
     * **内部方法** 根据 Vo 新建 OrderPo
     *
     * @param customerId
     * @param orderNewVo
     * @return
     */
    private OrderPo createNewOrderPo(Long customerId, OrderNewVo orderNewVo) {
        OrderPo orderPo = new OrderPo();
        orderPo.setCustomerId(customerId);
        orderPo.setRegionId(orderNewVo.getRegionId());
        orderPo.setAddress(orderNewVo.getAddress());
        orderPo.setMobile(orderNewVo.getMobile());
        orderPo.setMessage(orderNewVo.getMessage());
        orderPo.setConsignee(orderNewVo.getConsignee());
        // 订单种类为团购/预售订单，订单状态为待支付
        orderPo.setState(OrderStatus.PENDING_PAY.getCode()); // 普通订单没有 subState
        orderPo.setSubstate(OrderChildStatus.NEW.getCode()); //
        return orderPo;
    }

    static String addBackScript = " local buyNum = tonumber(ARGV[1]) " +
            " local skuKey = KEYS[1] " +
            " redis.call('add', skuKey, buyNum) " +
            " return ARGV[1] " +
            " end ";
    static String decrScript = " local buyNum = tonumber(ARGV[1]) " +
            " local skuKey = KEYS[1] " +
            " local skuStock = tonumber(redis.call('get', skuKey)) " +
            " if skuStock >= buyNum " +
            " then redis.call('decrby', skuKey, buyNum) " +
            " return ARGV[1] " +
            " else " +
            " return '0' " +
            " end ";
    // 普通商品库存过期时间
    @Value("${orders.ordinary-stock-expire}")
    private Integer stockExpireTime;
    // 写回信号量过期时间
    @Value("${orders.write-back-semaphore-expire}")
    private Integer writeBackExpire;

    /**
     * ** 内部方法 ** 失败了充正库存
     *
     * @param skuStock
     */
    @RedisOptimized
    private void addBack(Map<Long, Integer> skuStock) {
        skuStock.forEach((skuId, stock) -> {
            String keyStock = "sk_" + skuId;
            // 加库存
            List<String> keys = new ArrayList<>(1);
            keys.add(keyStock);
            redisUtils.execute(addBackScript, keys, stock.toString());
        });
    }

    /**
     * **内部方法** 根据 OrderItemInfo 扣库存
     *
     * @param skuId
     * @param quantity
     * @return 0：成功；1：库存不足；2：失败
     */
    @RedisOptimized
    private int decreaseStock(Long skuId, Integer quantity, List<Long> writeBackQueue) {
        String keyStock = "sk_" + skuId;
        Integer stock = redisUtils.get(keyStock, Integer.class);
        if (null == stock) {
            // 没有库存，先 load 一下
            stock = iShopService.getStock(skuId).intValue();
            redisUtils.set(keyStock, stock, stockExpireTime);
        }
        // 先看看库存够不够先！否则亏了
        if (stock < quantity) {
            // 库存不足
            return 1;
        }
        // 够，就减库存
        List<String> keys = new ArrayList<>(1);
        keys.add(keyStock);
        String result = redisUtils.execute(decrScript, keys, quantity.toString());
        if (result != null && result.equals("0")) {
            // 扣库存不成功：库存不足
            return 1;
        } else if (result == null) {
            // 扣库存不成功：其他错误？
            return 2;
        }
        // 扣库存成功，然后发送更新库存信号
        String key = "wb_" + skuId;
        Boolean semWroteBack = redisUtils.get(key, Boolean.class);
        if (semWroteBack == null || !semWroteBack) {
            // 先放在等待队列，待事务完成后，再批量 write back
            writeBackQueue.add(skuId);
            // 信号量设为 1
            redisUtils.set(key, Boolean.TRUE, writeBackExpire);
        }
        return 0;
    }

    /**
     * **内部方法** 根据 OrderItemInfo 扣秒杀库存
     *
     * @param skuId
     * @param quantity
     * @return 0：成功；1：库存不足；2：失败
     */
    @RedisOptimized
    private int decreaseFlashSaleStock(Long skuId, Integer quantity) {
        String keyStock = "fs_" + skuId;
        Integer stock = redisUtils.get(keyStock, Integer.class);
        // 先看看库存够不够先！否则亏了
        if (stock < quantity) {
            // 库存不足
            return 1;
        }
        // 够，就减库存
        List<String> keys = new ArrayList<>(1);
        keys.add(keyStock);
        String result = redisUtils.execute(decrScript, keys, quantity.toString());
        if (result != null && result.equals("0")) {
            // 扣库存不成功：库存不足
            return 1;
        } else if (result == null) {
            // 扣库存不成功：其他错误？
            return 2;
        }
        // 扣秒杀库存成功
        return 0;
    }

    /**
     * **内部方法** 根据 OrderItemInfo 扣库存 (不发消息，仅适用于单品订单)
     *
     * @param skuId
     * @param quantity
     * @return 0：成功；1：库存不足；2：失败
     */
    @RedisOptimized
    private int decreaseStock(Long skuId, Integer quantity) {
        String keyStock = "sk_" + skuId;
        Integer stock = redisUtils.get(keyStock, Integer.class);
        if (null == stock) {
            // 没有库存，先 load 一下
            stock = iShopService.getStock(skuId).intValue();
            redisUtils.set(keyStock, stock, stockExpireTime);
        }
        // 先看看库存够不够先！否则亏了
        if (stock < quantity) {
            // 库存不足
            return 1;
        }
        // 够，就减库存
        List<String> keys = new ArrayList<>(1);
        keys.add(keyStock);
        String result = redisUtils.execute(decrScript, keys, quantity.toString());
        if (result != null && result.equals("0")) {
            // 扣库存不成功：库存不足
            return 1;
        } else if (result == null) {
            // 扣库存不成功：其他错误？
            return 1;
        }
        // 扣库存成功，然后发送更新库存信号
        String key = "wb_" + skuId;
        Boolean semWroteBack = redisUtils.get(key, Boolean.class);
        if (semWroteBack == null || !semWroteBack) {
            // 信号量设为 1
            redisUtils.set(key, Boolean.TRUE, writeBackExpire);
        }
        return 0;
    }


}