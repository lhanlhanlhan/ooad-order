package cn.edu.xmu.ooad.order.freight.model.bo.freight.impl;

import cn.edu.xmu.ooad.order.centre.model.FreightCalcItem;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModelRule;
import cn.edu.xmu.ooad.order.freight.model.po.WeightFreightModelPo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对应一个地区的运费模板规则
 *
 * @author Han Li
 * Created at 8/12/2020 12:21 上午
 * Modified by Han Li at 8/12/2020 12:21 上午
 */
@NoArgsConstructor
public class WeightFreightModelRule extends FreightModelRule {

    private long firstWeight;
    private long firstWeightFreight;
    private long tenPrice;
    private long fiftyPrice;
    private long hundredPrice;
    private long threeHundredPrice;
    private long abovePrice;

    public WeightFreightModelRule(WeightFreightModelPo po) {
        super(po.getId(), po.getFreightModelId(), po.getRegionId());

        this.firstWeight = po.getFirstWeight();
        this.firstWeightFreight = po.getFirstWeightFreight();
        this.tenPrice = po.getTenPrice();
        this.fiftyPrice = po.getFiftyPrice();
        this.hundredPrice = po.getHundredPrice();
        this.threeHundredPrice = po.getTrihunPrice();
        this.abovePrice = po.getAbovePrice();
    }

    @Override
    public long calcRegionalFreight(List<FreightCalcItem> itemVoList,
                                    List<SkuInfo> skuInfoList,
                                    Integer unit) {
        long freight = 0;
        // 1. 统计总重
        long totalWeight = 0;
        for (int i = 0; i < itemVoList.size(); i++) {
            FreightCalcItem itemVo = itemVoList.get(i);
            SkuInfo skuInfo = skuInfoList.get(i);
            // 获取商品重量加入总重
            totalWeight += skuInfo.getWeight() * itemVo.getCount();
        }
        // 3. 查找总重量所在之区间
        long totalWeightUnit = (long) Math.ceil(totalWeight / unit.doubleValue());   // 归一化计重单位 9/12/2020
        // 3.1 是否满足首重
        if (totalWeightUnit <= firstWeight) {
            return firstWeightFreight;
        }
        // 3.2 不是首重，查看各续重门槛是否满足
        freight += firstWeightFreight;
        // 3.2.1 计算超出首重部分重量 (已归一化)
        long deltaWeightUnit = totalWeightUnit - firstWeight;
        // 3.2.3 超出最高门槛
        if (totalWeight <= 10000) { // 10 kg 以内
            freight += deltaWeightUnit * tenPrice;
        } else if (totalWeight <= 50000) { // 50 kg 以内
            freight += deltaWeightUnit * fiftyPrice;
        } else if (totalWeight <= 100000) { // 100 kg 以内
            freight += deltaWeightUnit * hundredPrice;
        } else if (totalWeight <= 300000) { // 300 kg 以内
            freight += deltaWeightUnit * threeHundredPrice;
        } else { // 300 kg 以上
            freight += deltaWeightUnit * abovePrice;
        }
        return freight;
    }
}
