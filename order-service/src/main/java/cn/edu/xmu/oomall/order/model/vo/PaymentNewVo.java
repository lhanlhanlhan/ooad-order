package cn.edu.xmu.oomall.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

/**
 * 新建支付时的支付信息Vo (前端传入)
 * @author 苗新宇
 * Created at 30/11/2020 20:30
 * Modified by Han Li at 6/12/2020 11:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNewVo {
    @PositiveOrZero(message = "支付金额必须为 0 或正值")
    @NotNull(message = "你必须传入支付金额")
    private Long price;
    @NotBlank(message = "你必须指定支付方式")
    private String paymentPattern;
}
