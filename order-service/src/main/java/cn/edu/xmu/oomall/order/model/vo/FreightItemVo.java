package cn.edu.xmu.oomall.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 计算运费传入的订货详情的 Vo
 *
 * @author Chen kechun
 * Created at 25/11/2020 12:43 下午
 * Modified by Han Li at 25/11/2020 12:43 下午
 */
@Data
@AllArgsConstructor
public class FreightItemVo {
    public Integer skuId;
    public Integer count;
}
