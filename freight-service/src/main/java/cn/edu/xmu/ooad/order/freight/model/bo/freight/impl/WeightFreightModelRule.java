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
        double unitFloat = unit.longValue();
        // 3. 查找总重量所在之区间
        // 3.1 是否满足首重
        if (totalWeight <= firstWeight) { // 重量 (g) 在首重 (g) 范围内 (17/12/2020)
            return firstWeightFreight;
        }
        // 3.2 不是首重，查看各续重门槛是否满足
        freight += firstWeightFreight;
        // 3.2.1 计算超出首重部分重量 (已归一化)
        long deltaWeight = totalWeight - firstWeight; // 超出重量 (g)
        long deltaWeightUnitInSection;
        // 3.2.3 超出最高门槛

        if (totalWeight <= 10000) { // 10 kg 以内
            deltaWeightUnitInSection = (long) Math.ceil((deltaWeight - firstWeight) / unitFloat); // 在首重~10kg内的unit数
            freight += deltaWeightUnitInSection * tenPrice;
            return freight;
        } else if (firstWeight <= 10000) { // 10 kg 以上，但是首重比 10 kg 小，那先加上本区间满额运费 (10kg-首重kg kg)
            deltaWeightUnitInSection = (long) Math.ceil((10000 - firstWeight) / unitFloat);
            freight += deltaWeightUnitInSection * tenPrice;
        } // 首重比 10 kg 还重，那不要算这部分运费

        if (totalWeight <= 50000) { // 50 kg 以内
            deltaWeightUnitInSection = (long) Math.ceil((totalWeight - 10000) / unitFloat); // 在10kg~50kg内的unit数
            freight += deltaWeightUnitInSection * fiftyPrice;
            return freight;
        } else if (firstWeight <= 50000) { // 50 kg 以上，但是首重比 50 kg 小，那先加上本区间满额运费 (40 kg)
            deltaWeightUnitInSection = (long) Math.ceil(40000 / unitFloat);
            freight += deltaWeightUnitInSection * fiftyPrice;
        } // 首重比 50 kg 还重，那不要算这部分运费

        if (totalWeight <= 100000) { // 100 kg 以内
            deltaWeightUnitInSection = (long) Math.ceil((totalWeight - 50000) / unitFloat); // 在50kg~100kg内的unit数
            freight += deltaWeightUnitInSection * hundredPrice;
            return freight;
        } else if (firstWeight <= 100000) { // 100 kg 以上，但是首重比 100 kg 小，那先加上本区间满额运费 (50 kg)
            deltaWeightUnitInSection = (long) Math.ceil(50000 / unitFloat);
            freight += deltaWeightUnitInSection * hundredPrice;
        } // 首重比 100 kg 还重，那不要算这部分运费

        if (totalWeight <= 300000) { // 300 kg 以内
            deltaWeightUnitInSection = (long) Math.ceil((totalWeight - 100000) / unitFloat); // 在100kg~300kg内的unit数
            freight += deltaWeightUnitInSection * threeHundredPrice;
            return freight;
        } else if (firstWeight <= 300000) { // 300 kg 以上，但是首重比 300 kg 小，那先加上本区间满额运费 (200 kg)
            deltaWeightUnitInSection = (long) Math.ceil(200000 / unitFloat);
            freight += deltaWeightUnitInSection * threeHundredPrice;
        } // 首重比 300 kg 还重，那不要算这部分运费

        if (firstWeight > 300000) { // 首重比 300 kg 还重，那就算实重-首重的运费
            deltaWeightUnitInSection = (long) Math.ceil(deltaWeight / unitFloat); // 实重-首重的unit数
        } else { // 首重在 300 以下，这下好了，300 kg 之前的运费已经算了，现在算 300 以上部分的运费
            deltaWeightUnitInSection = (long) Math.ceil((totalWeight - 300000) / unitFloat); // 实重-300000的unit数
        }
        // 爷佛了
        freight += deltaWeightUnitInSection * abovePrice;
        return freight;
    }
}
