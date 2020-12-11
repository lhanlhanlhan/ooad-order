package cn.edu.xmu.ooad.goods.require;

import cn.edu.xmu.ooad.goods.require.model.FreightModelSimple;

public interface IFreightModelService {


    /**
     * 根据运费模板id 获取一个 运费模板的具体信息
     */
    FreightModelSimple getFreightModel(Long freightModelId);
}
