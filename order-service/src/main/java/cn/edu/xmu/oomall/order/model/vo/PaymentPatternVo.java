package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.enums.PayPattern;
import lombok.Data;

/**
 * @author Han Li
 * Created at 5/12/2020 3:07 下午
 * Modified by Han Li at 5/12/2020 3:07 下午
 */
@Data
public class PaymentPatternVo {

    private String payPattern;
    private String name;

    public PaymentPatternVo(PayPattern pp) {
        this.payPattern = pp.getCode();
        this.name = pp.getDesc();
    }
}