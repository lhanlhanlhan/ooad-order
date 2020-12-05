package cn.edu.xmu.oomall.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

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
    @Positive(message = "商品 SKU ID 必须为正值")
    private Long skuId;
    @Positive(message = "数量必须为正值")
    private Integer count;
}
