package cn.edu.xmu.ooad.order.require;

import cn.edu.xmu.ooad.order.require.models.PresaleActivityInfo;

public interface IPresaleService {

    /**
     * 获取一个 SKU 目前的预售活动
     * @param skuId skuId
     * @return 预售活动资料
     */
    PresaleActivityInfo getSkuPresaleActivity(Long skuId);

}
