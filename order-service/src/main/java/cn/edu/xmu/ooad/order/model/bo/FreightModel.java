package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.dao.FreightDao;
import cn.edu.xmu.ooad.order.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.model.po.PieceFreightModelPo;
import cn.edu.xmu.ooad.order.model.po.WeightFreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.model.vo.OrderItemVo;
import cn.edu.xmu.ooad.order.utils.SpringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运费模板业务类
 *
 * @author Chen Kechun
 * Created at 2/12/2020 4:44 下午
 * Modified by Han Li at 5/12/2020 1:44 下午
 */
@Data
@NoArgsConstructor
public class FreightModel implements Serializable {

    private Long id;
    private Integer unit;
    private String name;
    private Byte type;
    private Byte defaultModel;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FreightModel(FreightModelPo freightModelPo) {
        this.id = freightModelPo.getId();
        this.name = freightModelPo.getName();
        this.type = freightModelPo.getType();
        this.defaultModel = freightModelPo.getDefaultModel();
        this.gmtCreate = freightModelPo.getGmtCreate();
        this.gmtModified = freightModelPo.getGmtModified();
        this.unit = freightModelPo.getUnit();
    }

    /**
     * 用此运费模板，计算一批物品的总运费
     *
     * @param skuInfoList 后端获取到的购物清单对应的商品明细 (只要重量)
     * @param itemVoList  前段传来的购物清单
     * @return -1，存在某个商品不允许发货的地区；>=0：总运费
     */
    public long calcFreight(Long regionId, List<FreightOrderItemVo> itemVoList, List<Map<String, Object>> skuInfoList) {
        // 获取 Dao
        FreightDao dao = SpringUtils.getBean(FreightDao.class);
        long freight = 0;
        if (this.getType() == 0) {
            // 重量模板的计算
            // 1. 统计总重
            long totalWeight = 0;
            for (Map<String, Object> skuInfo : skuInfoList) {
                // 获取商品重量加入总重
                totalWeight += (Long) skuInfo.get("weight");
            }
            // 2. 获取对应地区之运费模板明细
            Long modelId = this.getId();
            WeightFreightModelPo weightModelPo = dao.getRegionWeightFreightModel(modelId, regionId);
            // 2.1 如果没有模板，不予计算运费，不允许发货
            if (weightModelPo == null) {
                return -1;
            }
            // 3. 查找总重量所在之区间 TODO - 问邱明：计费单位是 g 吗？先按 g 来写。
            // 3.1 是否满足首重
            long firstWeight = weightModelPo.getFirstWeight();
            long firstWeightPrice = weightModelPo.getFirstWeight();
            if (totalWeight <= firstWeight) {
                return firstWeightPrice;
            }
            // 3.2 不是首重，查看各续重门槛是否满足
            freight += firstWeightPrice;
            // 3.2.1 计算超出首重部分重量
            long deltaWeight = totalWeight - firstWeight;
            // 3.2.2 算超了几斤 (上取整)
            long deltaJin = (long) Math.ceil(deltaWeight / 500.0);
            // 3.2.3 超出最高门槛
            if (totalWeight <= 10000) { // 10 kg 以内
                freight += deltaJin * weightModelPo.getTenPrice();
            } else if (totalWeight <= 50000) { // 50 kg 以内
                freight += deltaJin * weightModelPo.getFiftyPrice();
            } else if (totalWeight <= 100000) { // 100 kg 以内
                freight += deltaJin * weightModelPo.getHundredPrice();
            } else if (totalWeight <= 300000) { // 300 kg 以内
                freight += deltaJin * weightModelPo.getTrihunPrice();
            } else { // 300 kg 以上
                freight += deltaJin * weightModelPo.getAbovePrice();
            }
        } else {
            // 件数模板的计算
            // 1. 统计总件数
            long totalCount = 0;
            for (FreightOrderItemVo itemVo : itemVoList) {
                // 获取商品购买件数加入总件数
                totalCount += itemVo.getCount();
            }
            // 2. 获取对应地区之运费模板明细
            Long modelId = this.getId();
            PieceFreightModelPo pieceModelPo = dao.getRegionPieceFreightModel(modelId, regionId);
            // 2.1 如果没有模板，不予计算运费，不允许发货
            if (pieceModelPo == null) {
                return -1;
            }
            // 3. 查找总件数所在之区间
            // 3.1 是否满足首件数
            long firstItems = pieceModelPo.getFirstItems();
            long firstItemsPrice = pieceModelPo.getFirstItemsPrice();
            if (totalCount <= firstItems) {
                return firstItemsPrice;
            }
            // 3.2 不是首件数，查看各续件数门槛是否满足
            freight += firstItemsPrice;
            // 3.2.1 计算超出首件数部分件数
            long deltaItems = totalCount - firstItems;
            // 3.2.2 计算超出部分超出了几个续件数
            long deltaItemsParts = (long) Math.ceil((double) deltaItems / pieceModelPo.getAdditionalItems());
            // 3.2.3 计算续费
            freight += deltaItemsParts * pieceModelPo.getAdditionalItemsPrice();
        }
        return freight;
    }

}
