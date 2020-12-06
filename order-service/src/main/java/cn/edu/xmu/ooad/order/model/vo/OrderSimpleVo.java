package cn.edu.xmu.ooad.order.model.vo;

import cn.edu.xmu.ooad.order.model.bo.Order;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单概要 (后端返回)
 *
 * @author Han Li
 * Created at 25/11/2020 12:47 下午
 * Modified by Han Li at 25/11/2020 12:47 下午
 */
@Data
public class OrderSimpleVo {
    private Long id;
    private Long customerId;
    private Long shopId;
    private Long pid;
    private Byte orderType;
    private Long freightPrice;
    private Long discountPrice;
    private Long originPrice;
    private Byte state;
    private Byte substate;
    private LocalDateTime gmtCreated;

    /**
     * 从 Order Bo 中提取所需信息，创建 概要 Vo
     *
     * @param order
     */
    public OrderSimpleVo(Order order) {
        this.id = order.getId();
        this.customerId = order.getCustomerId();
        this.shopId = order.getShopId();
        this.pid = order.getPid();
        this.orderType = order.getOrderType().getCode();
        this.freightPrice = order.getFreightPrice();
        this.discountPrice = order.getDiscountPrice();
        this.originPrice = order.getOriginPrice();
        this.state = order.getState();
        this.substate = order.getSubstate();
        this.gmtCreated = order.getGmtCreated();
    }
}
