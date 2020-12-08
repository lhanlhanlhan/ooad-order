package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.po.PieceFreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;

import java.util.List;
import java.util.Map;

/**
 * @author Han Li
 * Created at 8/12/2020 8:43 上午
 * Modified by Han Li at 8/12/2020 8:43 上午
 */
public class PieceFreightModelRule extends FreightModelRule {

    private final long firstItems;
    private final long firstItemsPrice;
    private final long additionalItems;
    private final long additionalItemsPrice;

    public PieceFreightModelRule(PieceFreightModelPo po) {
        super(po.getId(), po.getFreightModelId(), po.getRegionId());
        this.firstItems = po.getFirstItems();
        this.firstItemsPrice = po.getFirstItemsPrice();
        this.additionalItems = po.getAdditionalItems();
        this.additionalItemsPrice = po.getAdditionalItemsPrice();
    }

    @Override
    public long calcRegionalFreight(List<FreightOrderItemVo> itemVoList, List<Map<String, Object>> skuInfoList) {
        long freight = 0;
        // 1. 统计总件数
        long totalCount = 0;
        for (FreightOrderItemVo itemVo : itemVoList) {
            // 获取商品购买件数加入总件数
            totalCount += itemVo.getCount();
        }
        // 3. 查找总件数所在之区间
        // 3.1 是否满足首件数
        if (totalCount <= firstItems) {
            return firstItemsPrice;
        }
        // 3.2 不是首件数，查看各续件数门槛是否满足
        freight += firstItemsPrice;
        // 3.2.1 计算超出首件数部分件数
        long deltaItems = totalCount - firstItems;
        // 3.2.2 计算超出部分超出了几个续件数
        long deltaItemsParts = (long) Math.ceil((double) deltaItems / additionalItems);
        // 3.2.3 计算续费
        freight += deltaItemsParts * additionalItemsPrice;
        return freight;
    }
}
