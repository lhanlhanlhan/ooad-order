package cn.edu.xmu.ooad.order.centre.interfaces;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;

import java.util.List;
import java.util.Map;

public interface IFreightServiceInside {

    long calcFreight(Long regionId, List<FreightCalcItem> orderItemList, Map<Long, SkuInfo> skuInfoMap);

}
