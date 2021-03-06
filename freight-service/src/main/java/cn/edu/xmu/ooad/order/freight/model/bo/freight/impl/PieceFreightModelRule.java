package cn.edu.xmu.ooad.order.freight.model.bo.freight.impl;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModelRule;
import cn.edu.xmu.ooad.order.freight.model.po.PieceFreightModelPo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Han Li
 * Created at 8/12/2020 8:43 上午
 * Modified by Han Li at 8/12/2020 8:43 上午
 */
@NoArgsConstructor
public class PieceFreightModelRule extends FreightModelRule {

    private long firstItems;
    private long firstItemsPrice;
    private long additionalItems;
    private long additionalItemsPrice;

    public PieceFreightModelRule(PieceFreightModelPo po) {
        super(po.getId(), po.getFreightModelId(), po.getRegionId());
        this.firstItems = po.getFirstItems();
        this.firstItemsPrice = po.getFirstItemsPrice();
        this.additionalItems = po.getAdditionalItems();
        this.additionalItemsPrice = po.getAdditionalItemsPrice();
    }

    @Override
    public long calcRegionalFreight(List<FreightCalcItem> itemVoList,
                                    List<SkuInfo> skuInfoList,
                                    Integer unit) {
        long freight = 0;
        // 1. 统计总件数
        long totalCount = itemVoList
                .stream()
                .mapToLong(FreightCalcItem::getCount)
                .sum();
//        long realFirstItems = this.firstItems * unit; // 按“个”算的首件数 (17/12/2020)
        // 邱明：unit 不用乘了 20/12/2020
        // 3. 查找总件数所在之区间
        // 3.1 是否满足首件数
        if (totalCount <= firstItems) { // 没到首重
            return firstItemsPrice;
        }
        // 3.2 不是首件数，查看各续件数门槛是否满足
        freight += firstItemsPrice;
        // 3.2.1 计算超出首件数部分件数
        long deltaItems = totalCount - firstItems;
//        long realAddiItems = this.additionalItems * unit; // 按“个”算的续件数 (17/12/2020) // 邱明：unit 不用乘了 (20/12/2020)
        // 3.2.2 计算超出部分超出了几个续件数
        long deltaItemsParts = (long) Math.ceil((double) deltaItems / additionalItems);
        // 3.2.3 计算续费
        freight += deltaItemsParts * additionalItemsPrice;
        return freight;
    }
}
