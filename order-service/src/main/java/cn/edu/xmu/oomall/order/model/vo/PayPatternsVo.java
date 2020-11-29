package cn.edu.xmu.oomall.order.model.vo;

/**
 * 支付方式VO
 * @author 苗新宇
 * Created at 29/11/2020 11:05
 * Modified by  苗新宇 at  29/11/2020 11:05
 */

import lombok.Data;

@Data
public class PayPatternsVo {
    private String payPattern;
    private String name;

    public PayPatternsVo(String payPattern, String name ){
        this.payPattern=payPattern;
        this.name=name;
    }
}
