package cn.edu.xmu.oomall.order.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 退款单VO【共8个属性】
 * @author 苗新宇
 * Created at 03/12/2020 0:48
 * Modified by  苗新宇 at  03/12/2020 0:48
 */
@Data
@NoArgsConstructor
public class RefundVo {
    private Long id;
    private Long paymentId;
    private Long amount;
    private byte state;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Long orderId;
    private Long aftersaleId;

}
