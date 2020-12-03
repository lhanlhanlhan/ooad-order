package cn.edu.xmu.oomall.order.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

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
    private String name;
    private Byte type;
    private Integer unit;
}
