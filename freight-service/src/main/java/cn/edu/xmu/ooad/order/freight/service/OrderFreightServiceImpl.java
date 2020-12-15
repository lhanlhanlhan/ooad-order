package cn.edu.xmu.ooad.order.freight.service;

import cn.edu.xmu.ooad.order.centre.interfaces.IFreightServiceInside;
import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class OrderFreightServiceImpl implements IFreightServiceInside {

    @Autowired
    private FreightService freightService;

    @Override
    public long calcFreight(Long regionId, List<FreightCalcItem> orderItemList, Map<Long, SkuInfo> skuInfoMap) {
        return freightService.calcFreight(regionId, orderItemList, skuInfoMap);
    }
}
