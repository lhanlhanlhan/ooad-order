package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.enums.PaymentStatus;
import lombok.Data;

/**
 * @author 苗新宇
 * Created at 27/11/2020 9:05
 * Modified by  苗新宇 at  27/11/2020 9:05
 */
@Data
public class PaymentStatusVo {
    private int code;
    private String name;

    public PaymentStatusVo(PaymentStatus ps){
        this.code=ps.getCode();
        this.name=ps.getDesc();
    }

}
