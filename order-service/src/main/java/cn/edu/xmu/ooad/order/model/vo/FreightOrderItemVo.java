package cn.edu.xmu.ooad.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 计算运费传入的订货详情的 Vo (前端传入)
 *
 * @author Chen kechun
 * Created at 25/11/2020 12:43 下午
 * Modified by Han Li at 25/11/2020 12:43 下午
 */
@Data
@AllArgsConstructor
public class FreightOrderItemVo {
    @Positive(message = "商品 SKU ID 必须为正值")
    @NotNull(message = "你必须指定 SKU ID")
    private Long skuId;

    @Positive(message = "数量必须为正值")
    @NotNull(message = "你必须指定购买数量")
    private Integer count;

    // 订单部分需要，在新建订单计算运费时
    public FreightOrderItemVo(OrderItemVo orderItemVo) {
        this.skuId = orderItemVo.getSkuId();
        this.count = orderItemVo.getQuantity();
    }
}
