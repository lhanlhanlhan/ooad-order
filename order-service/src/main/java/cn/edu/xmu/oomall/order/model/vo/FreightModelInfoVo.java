package cn.edu.xmu.oomall.order.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 计算运费传入的订货详情的 Vo
 *
 * @author Chen Kechun
 * Created at 25/11/2020 17:01 下午
 * Modified by Han Li at 25/11/2020 17:01下午
 */
@Data
@AllArgsConstructor
public class FreightModelInfoVo {
    public String name;
    public Integer type;
    public Integer unit;
}
