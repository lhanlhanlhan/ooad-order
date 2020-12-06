package cn.edu.xmu.ooad.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * @author ：Chen Kechun
 * @date ：2020/12/2 23:58
 */
@Data
@AllArgsConstructor
public class FreightModelEditVo {
    private String name;

    @Positive(message = "计费单位必须大于 0")
    private Integer unit;
}
