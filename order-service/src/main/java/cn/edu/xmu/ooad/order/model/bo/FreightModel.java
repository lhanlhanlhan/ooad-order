package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.po.FreightModelPo;
import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运费模板业务类
 *
 * @author Chen Kechun
 * Created at 2/12/2020 4:44 下午
 * Modified by Han Li at 5/12/2020 1:44 下午
 */
@Data
@NoArgsConstructor
public abstract class FreightModel implements Serializable {

    private Long id;
    private Integer unit;
    private String name;
    private Byte type;
    private Byte defaultModel;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    /**
     * 拷贝构造！因为这个是会放 redis 里滴
     *
     * @param freightModelPo
     */
    protected FreightModel(FreightModelPo freightModelPo) {
        this.id = freightModelPo.getId();
        this.name = freightModelPo.getName();
        this.type = freightModelPo.getType();
        this.defaultModel = freightModelPo.getDefaultModel();
        this.gmtCreate = freightModelPo.getGmtCreate();
        this.gmtModified = freightModelPo.getGmtModified();
        this.unit = freightModelPo.getUnit();
    }

    public static FreightModel create(FreightModelPo freightModelPo) {
        switch (freightModelPo.getType()) {
            case 0: // 重量模板
                return new WeightFreightModel(freightModelPo);
            case 1: // 件数模板
                return new PieceFreightModel(freightModelPo);
            default:
                return null;
        }
    }

    /**
     * 用此运费模板，计算一批物品的总运费
     *
     * @param skuInfoList 后端获取到的购物清单对应的商品明细 (只要重量)
     * @param itemVoList  前段传来的购物清单
     * @return -1，存在某个商品不允许发货的地区；>=0：总运费
     * @author Han Li
     */
    public abstract long calcFreight(Long regionId,
                                     List<FreightOrderItemVo> itemVoList,
                                     List<SkuInfo> skuInfoList);

}
