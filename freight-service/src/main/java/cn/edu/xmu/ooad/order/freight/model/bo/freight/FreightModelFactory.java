package cn.edu.xmu.ooad.order.freight.model.bo.freight;

import cn.edu.xmu.ooad.order.freight.model.bo.freight.impl.PieceFreightModel;
import cn.edu.xmu.ooad.order.freight.model.bo.freight.impl.WeightFreightModel;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;

/**
 * @author Han Li
 * Created at 18/12/2020 3:41 下午
 * Modified by Han Li at 18/12/2020 3:41 下午
 */
public class FreightModelFactory {

    public static FreightModel make(FreightModelPo freightModelPo) {
        switch (freightModelPo.getType()) {
            case 0: // 重量模板
                return new WeightFreightModel(freightModelPo);
            case 1: // 件数模板
                return new PieceFreightModel(freightModelPo);
            default:
                return null;
        }
    }
}
