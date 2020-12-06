package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.bo.PaymentOrder;
import cn.edu.xmu.oomall.order.model.po.PaymentPo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付单VO【共12个属性】
 *
 * @author 苗新宇
 * Created at 30/11/2020 20:48
 * Modified by  苗新宇 at  30/11/2020 20:48
 */
//TODO - afterSaleId 如何获取？
@Data
@NoArgsConstructor
public class PaymentOrderVo {
    private Long id;
    private Long orderId;
    private Long amount;
    private Long actualAmount;
    private String paymentPattern;
    private LocalDateTime payTime;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Byte state;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Long afterSaleId;

    /**
     * 从PaymentOrder PO 中提取所需信息，创建VO【共12个属性】
     */
    public PaymentOrderVo(PaymentPo paymentPo) {
        this.id = paymentPo.getId();
        this.orderId = paymentPo.getOrderId();
        this.amount = paymentPo.getAmount();
        this.actualAmount = paymentPo.getActualAmount();
        this.payTime = paymentPo.getPayTime();
        this.beginTime = paymentPo.getBeginTime();
        this.endTime = paymentPo.getEndTime();
        this.state = paymentPo.getState();
        this.gmtCreate = paymentPo.getGmtCreate();
        this.gmtModified = paymentPo.getGmtModified();
        this.afterSaleId = paymentPo.getAftersaleId();
        this.paymentPattern = paymentPo.getPaymentPattern();
    }

}