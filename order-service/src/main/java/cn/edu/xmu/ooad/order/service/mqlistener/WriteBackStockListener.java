package cn.edu.xmu.ooad.order.service.mqlistener;

import cn.edu.xmu.ooad.order.require.IShopService;
import cn.edu.xmu.ooad.order.utils.RedisUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 写回库存 消息消费者
 *
 * @author Han Li
 * Created at 12/12/2020 4:35 下午
 * Modified by Han Li at 12/12/2020 4:35 下午
 */
@Service
@RocketMQMessageListener(
        consumerGroup = "order-dec-stock-group",
        topic = "order-write-back-stock-topic",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadMax = 30)
public class WriteBackStockListener implements RocketMQListener<Long> {

    private static final Logger logger = LoggerFactory.getLogger(WriteBackStockListener.class);

    @Resource
    private IShopService iShopService;

    // Redis 工具
    @Autowired
    private RedisUtils redisUtils;

    // 写回信号量过期时间
    @Value("${orders.write-back-semaphore-expire}")
    private Integer writeBackExpire;

    /**
     * 写回一个 skuId 目前在 redis 中的库存
     * @param skuId sku id
     */
    @Override
    public void onMessage(Long skuId) {
        // 把写回信号量置为 0
        String key = "wb_" + skuId;
        redisUtils.set(key, Boolean.FALSE, writeBackExpire);
        // 向 Redis 查询库存
        String keyStock = "sk_" + skuId;
        Long stock = redisUtils.get(keyStock, Long.class);
        if (stock == null) {
            // 完蛋子，库存丢了
            logger.error("完蛋子，库存丢了，没法写回！skuId=" + skuId);
        } else {
            // 库存写回数据库
            iShopService.setStock(skuId, stock);
        }
    }
}
