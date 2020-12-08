package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.po.WeightFreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 对应一个地区的运费模板规则
 *
 * @author Han Li
 * Created at 8/12/2020 12:21 上午
 * Modified by Han Li at 8/12/2020 12:21 上午
 */
public class WeightFreightModelRule extends FreightModelRule {

    private final long firstWeight;
    private final long firstWeightFreight;
    private final long tenPrice;
    private final long fiftyPrice;
    private final long hundredPrice;
    private final long threeHundredPrice;
    private final long abovePrice;

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
    public long calcRegionalFreight(List<FreightOrderItemVo> itemVoList, List<Map<String, Object>> skuInfoList) {
        long freight = 0;
        // 1. 统计总重
        long totalWeight = 0;
        for (int i = 0; i < itemVoList.size(); i++) {
            FreightOrderItemVo itemVo = itemVoList.get(i);
            Map<String, Object> skuInfo = skuInfoList.get(i);
            // 获取商品重量加入总重
            totalWeight += ((Long) skuInfo.get("weight")) * itemVo.getCount();
        }
        // 3. 查找总重量所在之区间 TODO - 问邱明：计费单位是 g 吗？先按 g 来写。
        // 3.1 是否满足首重
        if (totalWeight <= firstWeight) {
            return firstWeightFreight;
        }
        // 3.2 不是首重，查看各续重门槛是否满足
        freight += firstWeightFreight;
        // 3.2.1 计算超出首重部分重量
        long deltaWeight = totalWeight - firstWeight;
        // 3.2.2 算超了几斤 (上取整)
        long deltaJin = (long) Math.ceil(deltaWeight / 500.0);
        // 3.2.3 超出最高门槛
        if (totalWeight <= 10000) { // 10 kg 以内
            freight += deltaJin * tenPrice;
        } else if (totalWeight <= 50000) { // 50 kg 以内
            freight += deltaJin * fiftyPrice;
        } else if (totalWeight <= 100000) { // 100 kg 以内
            freight += deltaJin * hundredPrice;
        } else if (totalWeight <= 300000) { // 300 kg 以内
            freight += deltaJin * threeHundredPrice;
        } else { // 300 kg 以上
            freight += deltaJin * abovePrice;
        }
        return freight;
    }
}
