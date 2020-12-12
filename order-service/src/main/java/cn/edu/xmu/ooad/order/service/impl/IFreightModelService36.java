package cn.edu.xmu.ooad.order.service.impl;

import cn.edu.xmu.ooad.goods.require.IFreightModelService;
import cn.edu.xmu.ooad.goods.require.model.FreightModelSimple;
import cn.edu.xmu.ooad.order.dao.FreightDao;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 给 3-6 组实现的 运费模板
@DubboService
@Component // 注册为 spring 的 bean 否则没法用 Autowired
public class IFreightModelService36 implements IFreightModelService {

    @Autowired
    private FreightDao freightDao;

    @Override
    public FreightModelSimple getFreightModel(Long freightModelId) {
        // 我们自己的 model
        cn.edu.xmu.ooad.order.model.bo.FreightModel fm = freightDao.getFreightModel(freightModelId);
        if (fm == null) {
            return null;
        }
        // 换成他们要的 model
        return new FreightModelSimple(fm.getId(), fm.getUnit(), fm.getName(),
                fm.getType(), fm.getDefaultModel(), fm.getGmtCreate().toString(), fm.getGmtModified().toString());
    }
}
