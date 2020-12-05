package cn.edu.xmu.oomall.order.model.vo;

/**
 * 支付信息VO
 * @author 苗新宇
 * Created at 30/11/2020 20:30
 * Modified by  苗新宇 at  30/11/2020 20:30
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoVo {
    private Long price;
    private String paymentPattern;
}
