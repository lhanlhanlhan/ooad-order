package cn.edu.xmu.oomall.order.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 管理员定义店铺的运费模板的运费模板资料的 Vo
 *
 * @author Chen Kechun
 * Created at 25/11/2020 17:01 下午
 * Modified by Chen Kechun at 25/11/2020 17:01下午
 */
@Data
@AllArgsConstructor
public class FreightModelInfoVo {
    @NotBlank(message = "名字不能为空")
    private String name;
    @NotNull(message = "必须定义运费模板的种类")
    @Min(value = 0, message = "运费模板的种类必须介乎 0 (重量) 及 1 (件数) 之间")
    private Byte type;
    @NotNull(message = "必须定义运费模板的单位 (g/件)")
    @Positive(message = "单位必须大于 0")
    private Integer unit;
}
