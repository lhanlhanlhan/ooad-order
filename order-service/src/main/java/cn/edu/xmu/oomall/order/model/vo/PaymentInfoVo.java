package cn.edu.xmu.oomall.order.model.vo;

/**
 * 支付信息VO
 * @author 苗新宇
 * Created at 30/11/2020 20:30
 * Modified by  苗新宇 at  30/11/2020 20:30
 */
import lombok.Data;
@Data
public class PaymentInfoVo {
    private Long price;
    private String paymentPattern;

    public PaymentInfoVo(Long price, String paymentPattern){
        this.price=price;
        this.paymentPattern=paymentPattern;
    }

    /**
     * 通过价格和支付方式VO构造支付信息VO
     * @author 苗新宇
     * @param price
     * @param payPatternsVo
     */
    public PaymentInfoVo(Long price, PayPatternsVo payPatternsVo)
    {
        this.price=price;
        this.paymentPattern=payPatternsVo.getPayPattern();

    }
}
