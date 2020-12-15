package cn.edu.xmu.ooad.order.model.bo.freight.impl;

import cn.edu.xmu.ooad.order.dao.FreightDao;
import cn.edu.xmu.ooad.order.model.bo.freight.FreightModel;
import cn.edu.xmu.ooad.order.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import cn.edu.xmu.ooad.order.utils.SpringUtils;
import lombok.NoArgsConstructor;

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
                            List<FreightOrderItemVo> itemVoList,
                            List<SkuInfo> skuInfoList) {
        // 获取 Dao
        FreightDao dao = SpringUtils.getBean(FreightDao.class);
        // 1. 获取对应地区之运费模板明细
        Long modelId = this.getId();
        WeightFreightModelRule weightModel = dao.getWeightFreightModelRule(modelId, regionId);
        // 1.1 如果没有模板，不予计算运费，不允许发货
        if (weightModel == null) {
            return -1;
        }
        // 用这个地区的规则计算运费
        return weightModel.calcRegionalFreight(itemVoList, skuInfoList, this.getUnit());
    }

}
