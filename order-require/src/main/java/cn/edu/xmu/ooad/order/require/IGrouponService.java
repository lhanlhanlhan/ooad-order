package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.GrouponActivityInfo;
import cn.edu.xmu.ooad.order.require.models.PresaleActivityInfo;

public interface IGrouponService {

    /**
     * 获取一个 SKU 目前的团购活动
     * @param skuId skuId
     * @return 团购活动资料
     */
    GrouponActivityInfo getSkuGrouponActivity(Long skuId);

}
