package cn.edu.xmu.ooad.order.order.model.bo.freight;

import cn.edu.xmu.ooad.order.order.model.vo.FreightOrderItemVo;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Han Li
 * Created at 8/12/2020 12:21 上午
 * Modified by Han Li at 8/12/2020 12:21 上午
 */
@Data
@NoArgsConstructor
public abstract class FreightModelRule implements Serializable {
    private long id;
    private long freightModelId;
    private long regionId;

    protected FreightModelRule(long id, long freightModelId, long regionId) {
        this.id = id;
        this.freightModelId = freightModelId;
        this.regionId = regionId;
    }

    public abstract long calcRegionalFreight(List<FreightOrderItemVo> itemVoList,
                                             List<SkuInfo> skuInfoList,
                                             Integer unit);
}
