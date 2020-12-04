package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.po.FreightModelPo;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 运费模板查询后返回的结果
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
    private Boolean beDefault;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FreightModelVo(FreightModelPo po) {
        this.id = po.getId();
        this.name = po.getName();
        this.type = po.getType();
        Byte beDefault = po.getDefaultModel();
        this.beDefault = beDefault != null && (beDefault == 1);
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }
}