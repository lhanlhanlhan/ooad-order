package cn.edu.xmu.ooad.order.order.service.mqlistener;

import cn.edu.xmu.ooad.order.centre.annotations.RedisOptimized;
import cn.edu.xmu.ooad.order.centre.interfaces.IFreightServiceInside;
import cn.edu.xmu.ooad.order.centre.utils.APIReturnObject;
import cn.edu.xmu.ooad.order.centre.utils.RedisUtils;
import cn.edu.xmu.ooad.order.order.dao.OrderDao;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.model.po.OrderItemPo;
import cn.edu.xmu.ooad.order.order.model.po.OrderPo;
import cn.edu.xmu.ooad.order.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.order.service.mqlistener.model.CreateOrderDemand;
import cn.edu.xmu.ooad.order.order.service.mqproducer.MQService;
import cn.edu.xmu.ooad.order.require.*;
import cn.edu.xmu.ooad.util.JacksonUtil;
import cn.edu.xmu.ooad.util.ResponseCode;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @DubboReference(check = false)
    private IFreightServiceInside iFreightServiceInside;
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
            logger.debug("收到创建订单请求，sn=" + demand.getO().getOrderSn());
        }
        // 消费创建订单请求
        int ret;
        switch (demand.getType()) {
            case 0:
                // 普通订单
                ret = this.createNormalOrder(demand);
                break;
            case 1:
            case 2:
                // 预售订单
                // 团购订单
                ret = this.createOneItemOrder(demand);
                break;
            default:
                ret = -1;
                break;
        }
        if (ret != 0) {
            logger.error("创建订单失败，sn=" + demand.getO().getOrderSn());
        }
    }

    /**
     * MQ 消费服务 1：创建普通订单 (假定订单内的所有【优惠券】都是【用户已经拥有】的)
     *
     * @return 0：成功；1：库存不足；2：失败；3：运费无法计算；4：无此商品
     */
    @Transactional
    public int createNormalOrder(CreateOrderDemand demand) {
        OrderPo orderPo = demand.getO();
        // MyBatis: 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        List<OrderItemPo> orderItemPos = demand.getI();

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
        if (demand.getCouponId() != null) {
            try {
                iCouponService.setCouponUsed(demand.getCouponId());
            } catch (Exception e) {
                logger.error("无法联系优惠卷模块：错误 " + e.getMessage());
            }
        }

        // 下单成功，批量提交商品库存数据库写回，删购物车
        for (Long skuId : demand.getWriteBackQueue()) {
            mqService.sendWriteBackStockMessage(skuId);
            try {
                iOtherService.delCartItem(demand.getCId(), skuId);
            } catch (Exception e) {
                logger.error("联系购物车服务失败，错误：" + e.getMessage());
            }
        }

        // 返回成功
        return 0;
    }

    /**
     * MQ 消费服务 2：创建单品订单 (团购/预售)
     *
     * @return 0：成功；1：库存不足；2：失败；3：运费无法计算；4：无此商品；5：活动无效
     */
    @Transactional
    public int createOneItemOrder(CreateOrderDemand demand) {
        OrderPo orderPo = demand.getO();

        // MyBatis: 写入订单系统
        if (!insertOrderPo(orderPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }
        // 获取刚刚创建订单的 id
        Long orderId = orderPo.getId();

        // MyBatis: 填入刚刚创建的订单的 id，放入 orderItemPo 中，并且写入数据库
        OrderItemPo orderItemPo = demand.getO().getOrderItemList().get(0); // 转换为 po
        orderItemPo.setOrderId(orderId);
        orderItemPo.setBeShareId(null);

        // 记录进订单系统
        if (!insertOrderItemPo(orderItemPo)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 1;
        }

        // 下单成功，批量提交商品库存数据库写回
        mqService.sendWriteBackStockMessage(orderItemPo.getGoodsSkuId());
        try {
            iOtherService.delCartItem(demand.getCId(), orderItemPo.getGoodsSkuId());
        } catch (Exception e) {
            logger.error("联系购物车服务失败，错误：" + e.getMessage());
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
