package cn.edu.xmu.ooad.order.order.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * 更新订单信息 传值对象 (前端传入)
 *
 * @author Han Li
 * Created at 2020/11/5 15:48
 * Modified by Han Li at 2020/11/28 15:48
 **/
@Data
@ApiModel("卖家更新订单信息传值对象")
@NoArgsConstructor
@AllArgsConstructor
public class OrderShopEditVo {

    @NotBlank
    private String message;

}
