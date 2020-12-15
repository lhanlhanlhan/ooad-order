package cn.edu.xmu.ooad.order.order.service.mqproducer;

import cn.edu.xmu.ooad.order.order.enums.OrderType;
import cn.edu.xmu.ooad.order.order.model.vo.OrderNewVo;
import cn.edu.xmu.ooad.order.order.service.mqlistener.model.CreateOrderDemand;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 消息队列服务
 * 实现 ApplicationEventPublisherAware 是为了能够在收到事务成功提交后再发送讯息
 *
 * @author Han Li
 * Created at 12/12/2020 4:14 下午
 * Modified by Han Li at 12/12/2020 4:14 下午
 */
@Service
public class MQService implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(MQService.class);

    private static ApplicationEventPublisher eventPublisher;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        if (eventPublisher == null) {
            eventPublisher = applicationEventPublisher;
        }
    }

    // 发送设置库存讯息
    public void sendWriteBackStockMessage(Long skuId) {
        logger.info("已提交写库存回数据库讯息，等待下单事务完成再投递 skuId=" + skuId);
        rocketMQTemplate.asyncSend("order-write-back-stock-topic",
                MessageBuilder.withPayload(skuId).build(), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        logger.info("发送写回数据库讯息成功。skuId=" + skuId);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        logger.error("发送写回数据库讯息【失败】。skuId=" + skuId);
                    }
                });
    }

    // 发送下订单讯息
    public void sendCreateOrderInfo(CreateOrderDemand demand) {
        // 提交写回库存讯息
        String sn = demand.getO().getOrderSn();
        logger.info("已提交创建订单讯息 sn=" + sn);
        rocketMQTemplate.asyncSend("order-create-order-topic",
                MessageBuilder.withPayload(demand).build(), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        logger.info("发送创建订单讯息成功。sn=" + sn);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        logger.error("发送创建订单讯息【失败】。sn=" + sn);
                    }
                });
    }
}
