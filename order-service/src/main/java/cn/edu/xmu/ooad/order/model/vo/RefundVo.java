package cn.edu.xmu.ooad.order.model.vo;

import cn.edu.xmu.ooad.order.model.po.RefundPo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 退款单VO【共8个属性】 (后端返回)
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

    public RefundVo(RefundPo po) {
        this.id = po.getId();
        this.paymentId = po.getPaymentId();
        this.amount = po.getAmout();
        this.state = po.getState();
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
        this.orderId = po.getOrderId();
        this.aftersaleId = po.getAftersaleId();
    }
}
