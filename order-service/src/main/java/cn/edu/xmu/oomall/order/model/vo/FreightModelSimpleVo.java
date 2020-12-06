package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.po.FreightModelPo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运费模板概要的 Vo (后端返回)
 *
 * @author Chen kechun
 * Created at 25/11/2020 12:43 下午
 * Modified by Chen Kechun at 25/11/2020 12:43 下午
 */
@Data
@AllArgsConstructor
public class FreightModelSimpleVo {
    private Long id;
    private String name;
    private Byte type;
    private Byte be_default;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FreightModelSimpleVo(FreightModelPo freightModel) {
        this.id = freightModel.getId();
        this.name = freightModel.getName();
        this.type = freightModel.getType();
        this.be_default = freightModel.getDefaultModel();
        this.gmtCreate = freightModel.getGmtCreate();
        this.gmtModified = freightModel.getGmtModified();
    }

}
