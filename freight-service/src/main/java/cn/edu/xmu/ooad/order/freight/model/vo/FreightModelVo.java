package cn.edu.xmu.ooad.order.freight.model.vo;

import cn.edu.xmu.ooad.order.freight.model.bo.freight.FreightModel;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运费模板查询后返回的结果 (后端返回)
 *
 * @author Han Li
 * Created at 4/12/2020 6:15 下午
 * Modified by Han Li at 4/12/2020 6:15 下午
 */
@Data
public class FreightModelVo {
    private Long id;
    private String name;
    private Byte type;
    private Integer unit;
    private Boolean beDefault;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FreightModelVo(FreightModel po) {
        this.id = po.getId();
        this.name = po.getName();
        this.type = po.getType();
        this.unit = po.getUnit();
        Byte beDefault = po.getDefaultModel();
        this.beDefault = beDefault != null && (beDefault == 1);
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }

    public FreightModelVo(FreightModelPo po) {
        this.id = po.getId();
        this.name = po.getName();
        this.type = po.getType();
        this.unit = po.getUnit();
        Byte beDefault = po.getDefaultModel();
        this.beDefault = beDefault != null && (beDefault == 1);
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }
}
