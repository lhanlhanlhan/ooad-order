package cn.edu.xmu.ooad.order.service.impl;

import cn.edu.xmu.ooad.goods.require.IFlashSaleService;
import cn.edu.xmu.ooad.order.require.IOtherService;
import cn.edu.xmu.ooad.order.require.models.FlashSaleInfo;
import cn.edu.xmu.ooad.order.require.models.FlashSaleItemInfo;
import cn.edu.xmu.ooad.order.utils.RedisUtils;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@DubboService
@Component // 注册为 spring 的 bean 否则没法用 Autowired
public class IFlashSaleService36 implements IFlashSaleService {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(IFlashSaleService36.class);

    // 我们在商品模块安插的卧底~
    @DubboReference(check = false)
    private cn.edu.xmu.ooad.order.require.IFlashSaleService iFlashSaleService;

    @DubboReference(check = false)
    private IOtherService iOtherService;

    // Redis 工具
    @Autowired
    private RedisUtils redisUtils;

    // LOAD REDIS 秒杀
    @Override
    public void loadFlashSale(Long flashSaleId) {
        Thread loadThread = new Thread(new LoadFlashSaleThread(flashSaleId));
        loadThread.start();
    }

    /**
     * 这个线程用来 load 秒杀商品
     */
    @AllArgsConstructor
    private class LoadFlashSaleThread implements Runnable {
        private final Long flashSaleId;
        @Override
        public void run() {
            // 获取秒杀信息
            FlashSaleInfo fsInfo;
            logger.info("收到秒杀开始 load 库存请求，秒杀id=" + flashSaleId);
            try {
                fsInfo = iFlashSaleService.getFlashSale(flashSaleId);
            } catch (Exception e) {
                logger.error("秒杀信息获取失败！Exception: " + e.getMessage());
                return;
            }
            if (fsInfo == null) {
                logger.error("秒杀信息获取失败，因为商品模块报告错误 flashSaleId=" + flashSaleId);
                return;
            }
            // 获取时间段信息
            long flashLenSec;
            try {
                flashLenSec = iOtherService.getTimeSegLength(fsInfo.getId());
            } catch (Exception e) {
                logger.error("秒杀时间段获取失败！Exception: " + e.getMessage());
                return;
            }
            if (flashLenSec < 0) {
                logger.error("秒杀时间段获取失败，因为其他模块报告错误 flashSaleId=" + flashSaleId);
                return;
            }
            // 获取秒杀商品并放入 Redis
            List<FlashSaleItemInfo> itemInfoList;
            try {
                itemInfoList = iFlashSaleService.getFlashSaleItems(flashSaleId);
            } catch (Exception e) {
                logger.error("秒杀商品获取失败！Exception: " + e.getMessage());
                return;
            }
            itemInfoList.forEach(item -> {
                String priceKey = "fp_" + item.getSkuId();
                String quantityKey = "fq_" + item.getSkuId();
                logger.info("插入秒杀商品：skuId=" + item.getSkuId());
                redisUtils.set(priceKey, item.getPrice(), flashLenSec);
                redisUtils.set(quantityKey, item.getQuantity(), flashLenSec);
            });
            logger.info("秒杀商品获取成功！len=" + itemInfoList.size());
        }
    }
}
