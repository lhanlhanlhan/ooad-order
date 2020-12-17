package cn.edu.xmu.ooad.order.order.service;

import cn.edu.xmu.ooad.order.centre.annotations.RedisOptimized;
import cn.edu.xmu.ooad.order.centre.interfaces.IFreightServiceInside;
import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.Accessories;
import cn.edu.xmu.ooad.order.centre.utils.RedisUtils;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.bo.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.bo.order.impl.GrouponOrder;
import cn.edu.xmu.ooad.order.order.model.po.OrderEditPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
import cn.edu.xmu.ooad.order.order.model.vo.*;
import cn.edu.xmu.ooad.order.order.service.mqlistener.model.CreateOrderDemand;
import cn.edu.xmu.ooad.order.order.service.mqproducer.MQService;
import cn.edu.xmu.ooad.order.require.*;
import cn.edu.xmu.ooad.order.require.models.*;
import cn.edu.xmu.ooad.util.ResponseCode;
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
    @DubboReference(check = false)
    private IGrouponService iGrouponService;
    @DubboReference(check = false)
    private IPreSaleService iPreSaleService;
    // 普通商品库存过期时间
    @Value("${orders.ordinary-stock-expire}")
    private Integer stockExpireTime;
    // 写回信号量过期时间
    @Value("${orders.write-back-semaphore-expire}")
    private Integer writeBackExpire;

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
        // parse datetime
        LocalDateTime start = null, end = null;
        try {
            if (beginTime != null) {
                start = LocalDateTime.parse(beginTime);
            }
            if (endTime != null) {
                end = LocalDateTime.parse(endTime);
            }
        } catch (Exception e) {
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "起始/结束日期时间格式错误");
        }
        List<OrderSimpleVo> orders;
        Map<String, Object> returnObj = new HashMap<>();
        // 需要分页
        PageHelper.startPage(page, pageSize);
        // 调用 Dao 层
        PageInfo<OrderSimplePo> orderSimplePos = orderDao.getSimpleOrders(
                orderSn, state, start, end, customerId, null, false
        );
        if (orderSimplePos == null) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 转为业务对象列表
        orders = orderSimplePos.getList().stream()
                .map(OrderSimpleVo::new)
                .collect(Collectors.toList());
        // 用 Map 封装
        returnObj.put("page", orderSimplePos.getPageNum());
        returnObj.put("pageSize", orderSimplePos.getPageSize());
        returnObj.put("total", orderSimplePos.getTotal());
        returnObj.put("pages", orderSimplePos.getPages());
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
        // 先获取订单概要，看看是不是用户本人的
        Order simpOrder = orderDao.getSimpleOrder(id, false);
        if (simpOrder == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (simpOrder.getCustomerId() == null || !customerId.equals(simpOrder.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 獲取所請求之訂單的 Bo、Vo
        Order order = orderDao.getOrder(id, false);
        if (order == null) {
            // 捕獲到錯誤
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
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
     * @param orderBuyerEditVo 修改信息对象
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> buyerModifyOrder(Long id, Long customerId, OrderBuyerEditVo orderBuyerEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, false);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本人的
        if (!customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 检查是否修改过 [29/11/2020 - 邱明：这个规定取消]
        // 校验目的地地区是否一致？TODO

        // 检查订单状态是否允许
        if (!order.canModify()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
        }

        // 自定义修改字段
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setAddress(orderBuyerEditVo.getAddress());
        po.setConsignee(orderBuyerEditVo.getConsignee());
        po.setMobile(orderBuyerEditVo.getMobile());
        po.setRegionId(orderBuyerEditVo.getRegionId());

        return orderDao.modifyOrder(po);
    }

    /**
     * 服务 o4：买家删掉 / 取消订单
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
        Order order = orderDao.getSimpleOrder(id, false);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本人的
        if (!customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 检查订单状态是否允许
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
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
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
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, false);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本人的
        if (!customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 检查订单状态是否允许
        if (!order.canSign()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
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
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, false);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本人的
        if (!customerId.equals(order.getCustomerId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 检查订单状态是否允许
        if (!(order instanceof GrouponOrder)) {
            // TODO - 订单种类不被允许【403 返回】错误码？
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
        }
        if (!((GrouponOrder) order).canChangeToNormal()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
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
        // parse datetime
        LocalDateTime start = null, end = null;
        try {
            if (beginTime != null) {
                start = LocalDateTime.parse(beginTime);
            }
            if (endTime != null) {
                end = LocalDateTime.parse(endTime);
            }
        } catch (Exception e) {
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.FIELD_NOTVALID, "起始/结束日期时间格式错误");
        }
        List<OrderSimpleVo> orders;
        Map<String, Object> returnObj = new HashMap<>();
        // 需要分页
        PageHelper.startPage(page, pageSize);
        // 调用 Dao 层
        PageInfo<OrderSimplePo> orderSimplePos = orderDao.getSimpleOrders(
                orderSn, state, start, end, customerId, shopId, true);
        if (orderSimplePos == null) {
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
        // 转为业务对象列表
        orders = orderSimplePos.getList().stream()
                .map(OrderSimpleVo::new)
                .collect(Collectors.toList());
        // 用 Map 封装
        returnObj.put("page", orderSimplePos.getPageNum());
        returnObj.put("pageSize", orderSimplePos.getPageSize());
        returnObj.put("total", orderSimplePos.getTotal());
        returnObj.put("pages", orderSimplePos.getPages());
        returnObj.put("list", orders);
        return new APIReturnObject<>(returnObj);
    }

    /**
     * 服务 o9：店家修改订单信息 [DONE]
     *
     * @return cn.edu.xmu.ooad.order.centre.utils.APIReturnObject<?>
     * @author Han Li
     * Created at 28/11/2020 15:13
     * Created by Han Li at 28/11/2020 15:13
     */
    @Transactional // 涉及到写操作的是一个事务
    public APIReturnObject<?> shopModifyOrder(Long id, Long shopId, OrderShopEditVo orderShopEditVo) {
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本店的
        if (order.getShopId() != null && !order.getShopId().equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 检查订单状态是否允许修改
        if (!order.canModify()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
        }
        // 自定义修改字段
        OrderEditPo po = new OrderEditPo();
        po.setId(id);
        po.setMessage(orderShopEditVo.getMessage());

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
        // 先获取订单概要，看看是不是用户本人的
        Order simpOrder = orderDao.getSimpleOrder(id, false);
        if (simpOrder == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (simpOrder.getShopId() == null || !shopId.equals(simpOrder.getShopId())) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 獲取所請求之訂單的 Bo、Vo
        Order order = orderDao.getOrder(id, false);
        if (order == null) {
            // 捕獲到錯誤
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        }
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
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本店的
        if (order.getShopId() != null && !order.getShopId().equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        if (!order.canShopCancel()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
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
        // 查询订单，检查所有者、是否修改过、本来地址是否与新地址的地区一致
        Order order = orderDao.getSimpleOrder(id, true);
        if (order == null) {
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查是不是本店的
        if (order.getShopId() != null && !order.getShopId().equals(shopId)) {
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 创造更改体
        OrderEditPo delPo;
        // 检查订单状态是否允许
        if (!order.canDeliver()) {
            // 方法不被允许【403 返回】
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.ORDER_STATENOTALLOW);
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
                return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST, "商品不存在");
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
            SkuInfo skuInfo = skuInfoMap.get(orderItem.getId());
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
        if (totalFreight == -1) {
            logger.error("运费无法计算，orderSn=" + sn);
            return new APIReturnObject<>(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERR);
        } else if (totalFreight == -2) { // id 不存在
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.MODEL_ID_NOTEXIST);
        } else if (totalFreight == -3) { // 禁忌物品
            return new APIReturnObject<>(HttpStatus.FORBIDDEN, ResponseCode.REGION_NOT_REACH);
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
                        return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOTENOUGH, "库存不足");
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
                        return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOTENOUGH, "库存不足");
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
        OrderPo orderPo = createNewOrderPo(customerId, orderNewVo);
        {
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
            return new APIReturnObject<>(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_ID_NOTEXIST, "商品不存在");
        }

        // 查询团购、预售活动是否有效
        if (type == OrderType.GROUPON) {
            // 查询团购活动
            GrouponActivityInfo gai = iGrouponService.getSkuGrouponActivity(theItem.getSkuId());
            if (gai == null || !gai.getId().equals(orderNewVo.getGrouponId())) {
                // 团购活动无效
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.RESOURCE_ID_NOTEXIST, "团购活动无效");
            }
        } else {
            // 查询预售活动
            PreSaleActivityInfo psai = iPreSaleService.getSkuPreSaleActivity(theItem.getSkuId());
            if (psai == null || !psai.getId().equals(orderNewVo.getGrouponId())) {
                // 预售活动无效
                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.RESOURCE_ID_NOTEXIST, "预售活动无效");
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
            return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.SKU_NOTENOUGH);
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
