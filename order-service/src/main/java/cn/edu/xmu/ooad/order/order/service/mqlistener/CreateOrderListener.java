package cn.edu.xmu.ooad.order.order.service.mqlistener;

import cn.edu.xmu.ooad.order.centre.annotations.RedisOptimized;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.bo.order.OrderItem;
import cn.edu.xmu.ooad.order.order.model.bo.discount.BaseCouponDiscount;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderItemVo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.require.*;
import cn.edu.xmu.ooad.order.require.models.*;
import cn.edu.xmu.ooad.order.order.service.FreightService;
import cn.edu.xmu.ooad.order.order.service.mqlistener.model.CreateOrderDemand;
import cn.edu.xmu.ooad.order.order.service.mqproducer.MQService;
import cn.edu.xmu.ooad.order.centre.utils.RedisUtils;
import cn.edu.xmu.ooad.util.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * 创建订单 消息消费者
 *
 * @author Han Li
 * Created at 12/12/2020 4:35 下午
 * Modified by Han Li at 12/12/2020 4:35 下午
 */
@Service
@RocketMQMessageListener(
        consumerGroup = "order-create-order-group",
        topic = "order-create-order-topic",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadMax = 30)
public class CreateOrderListener implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderListener.class);
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
    private FreightService freightService;
    @DubboReference(check = false)
    private IShopService iShopService;
    @DubboReference(check = false)
    private ICouponService iCouponService;
    @DubboReference(check = false)
    private IGrouponService iGrouponService;
    @DubboReference(check = false)
    private IPreSaleService iPreSaleService;
    @DubboReference(check = false)
    private IOtherService iOtherService;

    @Autowired
    private OrderDao orderDao;
    // Redis 工具
    @Autowired
    private RedisUtils redisUtils;
    // MQ 服务
    @Autowired
    private MQService mqService;
    // 普通商品库存过期时间
    @Value("${orders.ordinary-stock-expire}")
    private Integer stockExpireTime;
    // 写回信号量过期时间
    @Value("${orders.write-back-semaphore-expire}")
    private Integer writeBackExpire;

    /**
     * 收到创建订单讯息
     *
     * @param message 讯息字符串
     */
    @Override
    public void onMessage(String message) {
        // 获取 创建订单 资讯
        CreateOrderDemand demand = JacksonUtil.toObj(message, CreateOrderDemand.class);
        if (logger.isDebugEnabled()) {
            logger.debug("收到创建订单请求，type=" + demand.getType() + " sn=" + demand.getSn() + " cid=" + demand.getCustomerId() + " vo=" + demand.getOrderNewVo());
        }
        // 消费创建订单请求
        int ret;
        switch (demand.getType()) {
            case 0:
                // 普通订单
                ret = this.createNormalOrder(demand.getCustomerId(), demand.getOrderNewVo(), demand.getSn());
                break;
            case 1:
                // 团购订单
                ret = this.createOneItemOrder(demand.getCustomerId(), demand.getOrderNewVo(), demand.getSn(), OrderType.GROUPON);
                break;
            case 2:
                // 预售订单
                ret = this.createOneItemOrder(demand.getCustomerId(), demand.getOrderNewVo(), demand.getSn(), OrderType.PRE_SALE);
                break;
            default:
                ret = -1;
                break;
        }
        if (ret != 0) {
            logger.error("创建订单失败，sn=" + demand.getSn() + " vo=" + demand.getOrderNewVo());
        }
    }

    /**
     * MQ 消费服务 1：创建普通订单 (假定订单内的所有【优惠券】都是【用户已经拥有】的)
     *
     * @param customerId 用户 ID
     * @param orderNewVo 新订单 Vo
     * @return 0：成功；1：库存不足；2：失败；3：运费无法计算；4：无此商品
     */
    @Transactional
    public int createNormalOrder(Long customerId, OrderNewVo orderNewVo, String sn) {

        // 下单此刻时间
        LocalDateTime nowTime = LocalDateTime.now();

        // 在运算运费前，要提前获得商品模块 sku 信息，否则重复获取 sku 信息
        Map<Long, SkuInfo> skuInfoMap = new HashMap<>(orderNewVo.getOrderItems().size());
        for (OrderItemVo orderItemVo : orderNewVo.getOrderItems()) {
            SkuInfo skuInfo = iShopService.getSkuInfo(orderItemVo.getSkuId());
            if (skuInfo == null) {
                logger.debug("查无此商品, skuId=" + orderItemVo.getSkuId()); // TODO - 数据库穿透？
                return 4;
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
                    return 2;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("优惠活动优惠规则创建失败！couponActId=" + cai.getId() + " exception=" + e.getMessage());
                    return 2;
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
                return 2;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("优惠券优惠规则创建失败！couponId=" + couponInfo.getId() + " exception=" + e.getMessage());
                return 2;
            }
            // 计算优惠金额
            couponDiscount.calcAndSetDiscount(couponSuitableOrderItems);
        }

        // 计算运费
        Long regionId = orderNewVo.getRegionId();
        long totalFreight = freightService.calcFreight(regionId, orderItems
                .stream()
                .map(FreightOrderItemVo::new)
                .collect(Collectors.toList()), skuInfoMap);
        if (totalFreight < 0) {
            return 3; // 运费无法计算
        }

        /* 以下是数据库部分 */

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
                    return decStockRes;
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
                    return decStockRes;
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

        // MyBatis: 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // MyBatis: 填入刚刚创建的订单的 id，放入所有 orderItemPo 中，并且写入数据库
        for (OrderItemPo itemPo : orderItemPos) {
            itemPo.setOrderId(orderId);
            itemPo.setBeShareId(null);
            // 记录进订单系统
            if (!insertOrderItemPo(itemPo)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return 1;
            }
        }

        // 核销优惠券 (如有)
        if (couponInfo != null) {
            iCouponService.setCouponUsed(couponInfo.getId());
        }

        // 下单成功，批量提交商品库存数据库写回，删购物车
        for (Long skuId : writeBackQueue) {
            mqService.sendWriteBackStockMessage(skuId);
            iOtherService.delCartItem(customerId, skuId);
        }

        // 返回成功
        return 0;
    }

    /**
     * MQ 消费服务 2：创建单品订单 (团购/预售)
     *
     * @param orderNewVo 新订单申请
     * @return 0：成功；1：库存不足；2：失败；3：运费无法计算；4：无此商品；5：活动无效
     */
    @Transactional
    public int createOneItemOrder(Long customerId, OrderNewVo orderNewVo, String sn, OrderType type) {
        // 下单此刻时间
        LocalDateTime nowTime = LocalDateTime.now();

        // 订单项转为业务对象
        OrderItem theItem = new OrderItem(orderNewVo.getOrderItems().get(0));
        SkuInfo skuInfo = iShopService.getSkuInfo(theItem.getSkuId());
        if (skuInfo == null) {
            logger.debug("查无此商品, skuId=" + theItem.getSkuId()); // TODO - 数据库穿透？
            return 4;
        }

        // 查询团购、预售活动是否有效
        if (type == OrderType.GROUPON) {
            // 查询团购活动
            GrouponActivityInfo gai = iGrouponService.getSkuGrouponActivity(theItem.getSkuId());
            if (gai == null || !gai.getId().equals(orderNewVo.getGrouponId())) {
                // 团购活动无效
                return 5;
            }
        } else {
            // 查询预售活动
            PreSaleActivityInfo psai = iPreSaleService.getSkuPreSaleActivity(theItem.getSkuId());
            if (psai == null || !psai.getId().equals(orderNewVo.getGrouponId())) {
                // 预售活动无效
                return 5;
            }
        }

        Map<Long, SkuInfo> skuInfoMap = new HashMap<>(orderNewVo.getOrderItems().size());
        skuInfoMap.put(skuInfo.getId(), skuInfo);

        // 计算运费
        Long regionId = orderNewVo.getRegionId();
        List<FreightOrderItemVo> freightItemVo = new ArrayList<>(1);
        freightItemVo.add(new FreightOrderItemVo(theItem));
        long totalFreight = freightService.calcFreight(regionId, freightItemVo, skuInfoMap);
        if (totalFreight < 0) {
            return 3; // 运费无法计算
        }

        /* 以下是数据库部分 */

        // Redis: 下单，扣库存、累加计算各种价格
        int decStockRes;
        decStockRes = decreaseStock(theItem.getSkuId(), theItem.getQuantity());
        if (decStockRes != 0) {
            // 下单失败
            return decStockRes;
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

        // MyBatis: 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // MyBatis: 填入刚刚创建的订单的 id，放入 orderItemPo 中，并且写入数据库
        OrderItemPo orderItemPo = theItem.toPo(); // 转换为 po
        orderItemPo.setOrderId(orderId);
        orderItemPo.setBeShareId(null);

        // 记录进订单系统
        if (!insertOrderItemPo(orderItemPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }

        // 下单成功，批量提交商品库存数据库写回
        mqService.sendWriteBackStockMessage(theItem.getSkuId());
        iOtherService.delCartItem(customerId, theItem.getSkuId());

        return 0;
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
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            return false;
        }
    }
}
