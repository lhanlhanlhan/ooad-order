package cn.edu.xmu.ooad.order.service.listener;

import cn.edu.xmu.ooad.order.connector.service.ShopService;
import cn.edu.xmu.ooad.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.enums.OrderType;
import cn.edu.xmu.ooad.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.model.vo.OrderItemVo;
import cn.edu.xmu.ooad.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.order.service.FreightService;
import cn.edu.xmu.ooad.order.service.listener.model.CreateOrderDemand;
import cn.edu.xmu.ooad.order.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.utils.Accessories;
import cn.edu.xmu.ooad.order.utils.ResponseCode;
import cn.edu.xmu.ooad.util.JacksonUtil;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        consumerGroup = "order-group",
        topic = "order-create-order-topic",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadMax = 30)
public class CreateOrderListener implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderListener.class);

    @Autowired
    private FreightService freightService;

    @Resource
    private IShopService iShopService;

    @Autowired
    private OrderDao orderDao;

    /**
     * 收到创建订单讯息
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
        int ret = -1;
        switch (demand.getType()) {
            case 0:
                // 普通订单
                ret = this.createNormalOrder(demand.getCustomerId(), demand.getOrderNewVo(), demand.getSn());
                break;
            case 1:
                // TODO - 团购订单
                break;
            case 2:
                // TODO - 预售订单
                break;
            default:
                break;
        }
        if (ret != 0) {
            logger.error("创建订单失败，sn=" + demand.getSn() + " vo=" + demand.getOrderNewVo());
        }
    }

    /**
     * MQ 消费服务 1：创建普通订单 (假定订单内的所有【优惠券】都是【用户已经拥有】的)
     * @param customerId 用户 ID
     * @param orderNewVo 新订单 Vo
     * @return 0：成功；1：失败
     */
    @Transactional
    public int createNormalOrder(Long customerId, OrderNewVo orderNewVo, String sn) {
        // TODO - 秒杀的认定

        List<OrderItemVo> orderItemVos = orderNewVo.getOrderItems();

        // 获取优惠券 TODO - 优惠活动金额的计算，用 core 模块提供的模板，可能有各种返回值

        int calcRet = 0;
        if (calcRet != 0) {
            // TODO - 计算出错，返回对应错误
            return 1;
        }

        // 计算运费
        Long regionId = orderNewVo.getRegionId();
        APIReturnObject<?> freightCalcRes = freightService.calcFreight(regionId,
                orderItemVos.stream()
                        .map(FreightOrderItemVo::new)
                        .collect(Collectors.toList()));
        if (freightCalcRes.getCode() != ResponseCode.OK) {
            return 1;
        }
        Long totalFreight = (Long) freightCalcRes.getData();

        // TODO - 下单，扣库存
//        for (Map<String, Object> itemInfo : orderItems) {
//            if (!decreaseStock(itemInfo)) {
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return new APIReturnObject<>(HttpStatus.BAD_REQUEST, ResponseCode.GOODS_NOT_IN_STOCK);
//            }
//        }

        // TODO - 核销优惠券

        /* 以下是数据库部分 */

        LocalDateTime nowTime = LocalDateTime.now();

        // 计算各商品的价格及其对应 Po
        List<OrderItemPo> orderItemPos = new ArrayList<>(orderItemVos.size());
        long totalPrice = 0L;
        long totalDiscount = 0L;
        for (OrderItemVo item : orderItemVos) {
            Long skuId = item.getSkuId();
            Integer quantity = item.getQuantity();
            // 创建新 po，设置除了 orderId、beSharedId 以外的资料
            OrderItemPo orderItemPo = new OrderItemPo();
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 联系商品模块获取商品资料
            SkuInfo skuInfo = iShopService.getSkuInfo(skuId);
            orderItemPo.setGoodsSkuId(skuId);
            orderItemPo.setQuantity(quantity);
            // 计算、累加各种价格
            Long price = skuInfo.getPrice();
            totalPrice += price * quantity;
            Long discount = item.getDiscount();
            totalDiscount += discount;
            // 填写各种价格
            orderItemPo.setPrice(price);
            orderItemPo.setDiscount(discount);
            orderItemPo.setName(skuInfo.getName());
            orderItemPo.setGmtCreate(nowTime);
            // 填寫各種活動
            orderItemPo.setCouponActivityId(item.getCouponActId());
            // 放入容器
            orderItemPos.add(orderItemPo);
        }

        // 创建订单对应 Vo
        OrderPo orderPo = createNewOrderPo(customerId, orderNewVo);
        orderPo.setShopId(null); // 店铺 id 暂时为空，等支付后分单再说
        // 填入订单的各种价格
        orderPo.setOriginPrice(totalPrice);
        orderPo.setDiscountPrice(totalDiscount);
        orderPo.setFreightPrice(totalFreight);
        // 订单种类为普通订单，订单状态为待支付
        orderPo.setOrderType(OrderType.NORMAL.getCode());
        orderPo.setState(OrderStatus.PENDING_PAY.getCode()); // 普通订单没有 subState
        orderPo.setGmtCreate(nowTime);
        orderPo.setOrderSn(sn);

        // 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
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
                return 1;
            }
        }

        // 返回成功
        return 0;
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
        return orderPo;
    }

    /**
     * **内部方法** 根据 OrderItemInfo 扣库存
     * // TODO - redis
     *
     * @param itemInfo
     * @return
     */
    private boolean decreaseStock(Map<String, Object> itemInfo) {
        Long skuId = (Long) itemInfo.get("skuId");
        Integer quantity = (Integer) itemInfo.get("quantity");
        int decStatus = iShopService.decreaseStock(skuId, quantity);
        if (decStatus == 1) {
            // 库存不足
            logger.warn("not in stock: skuid=" + skuId);
            return false;
        }
        return true;
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
