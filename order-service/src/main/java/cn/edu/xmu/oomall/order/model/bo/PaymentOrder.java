package cn.edu.xmu.oomall.order.model.bo;

import cn.edu.xmu.oomall.order.model.po.PaymentPo;
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
    private Byte paymentPattern;
    private LocalDateTime payTime;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Byte state;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Long afterSaleId;

    /**
     * 使用一个PO对象构造本对象
     *
     * @param item Po对象
     */
    public PaymentOrder(PaymentPo item) {
        this.id = item.getId();
        this.orderId = item.getOrderId();
        this.amount = item.getAmount();
        this.actualAmount = item.getActualAmount();
        this.paymentPattern = item.getPaymentPattern();
        this.payTime = item.getPayTime();
        this.beginTime = item.getBeginTime();
        this.endTime = item.getEndTime();
        this.state = item.getState();
        this.gmtCreate = item.getGmtCreate();
        this.gmtModified = item.getGmtModified();
        this.afterSaleId = item.getAftersaleId();
    }

    /**
     * 创建支付单Vo对象
     * @return 支付单Vo对象
     */
    public PaymentOrderVo createSimpleVo(){
        return new PaymentOrderVo(this);
    }
}
