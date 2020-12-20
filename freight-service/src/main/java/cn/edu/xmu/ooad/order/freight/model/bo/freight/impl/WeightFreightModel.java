package cn.edu.xmu.ooad.order.freight.model.bo.freight.impl;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.GlobalVariables;
import cn.edu.xmu.ooad.order.centre.utils.SpringUtils;
import cn.edu.xmu.ooad.order.freight.dao.FreightDao;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModel;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.require.models.RegionInfo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 重量运费模板业务类
 *
 * @author Han Li
 * Created at 7/12/2020 11:58 下午
 * Modified by Han Li at 7/12/2020 11:58 下午
 */
@NoArgsConstructor
public class WeightFreightModel extends FreightModel {

    public WeightFreightModel(FreightModelPo po) {
        super(po);
    }

    @Override
    public long calcFreight(Long regionId,
                            List<FreightCalcItem> itemVoList,
                            List<SkuInfo> skuInfoList) {
        // 获取 Dao
        FreightDao dao = SpringUtils.getBean(FreightDao.class);
        // 1. 获取对应地区之运费模板明细
        Long modelId = this.getId();
        WeightFreightModelRule weightModel = dao.getWeightFreightModelRule(modelId, regionId);
        // 1.1 如果没有模板，不予计算运费，不允许发货
        // 1.1 如果该地区未有模板，就查找它的父地区模板，直到找到地区编码为 0 为止 [邱明：20/12/2020]
        while (weightModel == null) {
            RegionInfo info = GlobalVariables.regionInfoMap.get(regionId);
            if (info == null) {
                return -1; // 到顶啦 禁止寄往该地区
            }
            // 获取父地区的模板
            regionId = info.getPid();
            weightModel = dao.getWeightFreightModelRule(modelId, regionId);
        }
        // 用这个地区的规则计算运费
        return weightModel.calcRegionalFreight(itemVoList, skuInfoList, this.getUnit());
    }

}
