package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.enums.PayPattern;
import cn.edu.xmu.oomall.order.model.vo.PaymentOrderVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 根据PaytmentPo对象创建支付单
 *
 * @author 苗新宇
 * Created at 30/11/2020 21:23
 * Modified by  苗新宇 at  30/11/2020 21:23
 */
@Data
@NoArgsConstructor
public class PaymentOrder {
    private Long id;
    private Long orderId;
    private Long amount;
    private Long actualAmount;
    private PayPattern paymentPattern;
    private LocalDateTime payTime;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Byte state;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Long afterSaleId;
}
