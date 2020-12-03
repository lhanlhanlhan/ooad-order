package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.model.po.FreightModelPo;

import java.time.LocalDateTime;

/**
 * 运费模板
 *
 * @author Chen Kechun
 * Created at 2/12/2020 4:44 下午
 * Modified by Chen Kechun at 2/12/2020 4:44 下午
 */
public class FreightModel {


    private FreightModelPo freightModelPo;

    public FreightModel(FreightModelPo freightModelPo) {
        this.freightModelPo = freightModelPo;
    }

    /**
     * Getters
     */
    public Long getId() {
        return freightModelPo.getId();
    }

    public String getName() {
        return freightModelPo.getName();
    }

    public Byte getType() {
        return freightModelPo.getType();
    }

    public Byte getBe_default() {
        return freightModelPo.getDefaultModel();
    }

    public LocalDateTime getGmtCreate() {
        return freightModelPo.getGmtCreate();
    }

    public LocalDateTime getGmtModified() {
        return freightModelPo.getGmtModified();
    }

}
