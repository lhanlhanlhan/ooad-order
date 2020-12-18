package cn.edu.xmu.ooad.order.freight.model.vo;

import cn.edu.xmu.ooad.order.centre.utils.Constants;
import cn.edu.xmu.ooad.order.freight.model.po.FreightModelPo;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Integer unit;
    private Boolean defaultModel;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FreightModelSimpleVo(FreightModelPo freightModel) {
        this.id = freightModel.getId();
        this.name = freightModel.getName();
        this.type = freightModel.getType();
        this.unit = freightModel.getUnit();
        Byte beDefault = freightModel.getDefaultModel();
        this.defaultModel = beDefault != null && (beDefault == 1);
        this.gmtCreate = freightModel.getGmtCreate();
        this.gmtModified = freightModel.getGmtModified();
    }

}
