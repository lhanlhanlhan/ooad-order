package cn.edu.xmu.ooad.order.freight.model.bo.freight.impl;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.centre.utils.SpringUtils;
import cn.edu.xmu.ooad.order.freight.dao.FreightDao;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModel;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单品运费模板
 *
 * @author Han Li
 * Created at 8/12/2020 12:01 上午
 * Modified by Han Li at 8/12/2020 12:01 上午
 */
@NoArgsConstructor
public class PieceFreightModel extends FreightModel {

    public PieceFreightModel(FreightModelPo po) {
        super(po);
    }

    @Override
    public long calcFreight(Long regionId,
                            List<FreightCalcItem> itemVoList,
                            List<SkuInfo> skuInfoList) {
        // 获取 Dao
        FreightDao dao = SpringUtils.getBean(FreightDao.class);
        // 2. 获取对应地区之运费模板明细
        Long modelId = this.getId();
        PieceFreightModelRule pieceModel = dao.getPieceFreightModelRule(modelId, regionId);
        // 2.1 如果没有模板，不予计算运费，不允许发货
        if (pieceModel == null) {
            return -1;
        }
        // 用这个地区的规则计算运费
        return pieceModel.calcRegionalFreight(itemVoList, skuInfoList, this.getUnit());
    }

}