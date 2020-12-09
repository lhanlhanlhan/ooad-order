package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.FlashSaleItemInfo;

import java.util.List;

public interface IFlashSaleService {

    /**
     * 根据秒杀时间段 id 获取那个时间段的所有秒杀商品
     * @param timeSegId 时间段 id
     * @return 该秒杀时间段的所有秒杀商品
     */
    List<FlashSaleItemInfo> getFlashSaleItems(Long timeSegId);

}
