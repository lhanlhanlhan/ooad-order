package cn.edu.xmu.ooad.order.service.mqproducer;

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


}
