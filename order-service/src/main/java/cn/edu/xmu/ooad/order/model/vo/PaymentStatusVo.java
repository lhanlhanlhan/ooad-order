package cn.edu.xmu.ooad.order.model.vo;

import cn.edu.xmu.ooad.order.enums.PaymentStatus;
import lombok.Data;

/**
 * 支付单状态 (后端返回)
 *
 * @author Han Li
 * Created at 5/12/2020 3:01 下午
 * Modified by Han Li at 5/12/2020 3:01 下午
 */
@Data
public class PaymentStatusVo {

    private int code;
    private String name;

    public PaymentStatusVo(PaymentStatus ps) {
        this.code = ps.getCode();
        this.name = ps.getDesc();
    }
}
