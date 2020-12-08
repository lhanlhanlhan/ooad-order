package cn.edu.xmu.ooad.order.model.bo;

import cn.edu.xmu.ooad.order.model.vo.FreightOrderItemVo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Han Li
 * Created at 8/12/2020 12:21 上午
 * Modified by Han Li at 8/12/2020 12:21 上午
 */
public abstract class FreightModelRule implements Serializable {
    private final long id;
    private final long freightModelId;
    private final long regionId;

    protected FreightModelRule(long id, long freightModelId, long regionId) {
        this.id = id;
        this.freightModelId = freightModelId;
        this.regionId = regionId;
    }

    public abstract long calcRegionalFreight(List<FreightOrderItemVo> itemVoList, List<Map<String, Object>> skuInfoList);
}
