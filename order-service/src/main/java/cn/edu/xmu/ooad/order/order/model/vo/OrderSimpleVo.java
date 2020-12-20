package cn.edu.xmu.ooad.order.order.model.vo;

import cn.edu.xmu.ooad.order.centre.utils.Constants;
import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.order.model.po.OrderSimplePo;
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
    private Byte subState;
    private String gmtCreate;
    private Long grouponId;
    private Long presaleId;
    private String shipmentSn;

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
        OrderStatus state = order.getState();
        OrderChildStatus subState = order.getSubstate();
        this.state = state == null ? null : state.getCode();
        this.subState = order.getSubstate() == null ? null : order.getSubstate().getCode();
        this.gmtCreate = order.getGmtCreated().format(Constants.outDateTimeFormatter);

        this.grouponId = order.getGrouponId();
        this.presaleId = order.getPresaleId();
        this.shipmentSn = order.getShipmentSn();
    }

    /**
     * 直接从数据库对象 Order Simple Po 中提取所需信息，创建 概要 Vo
     *
     * @param order
     */
    public OrderSimpleVo(OrderSimplePo order) {
        this.id = order.getId();
        this.customerId = order.getCustomerId();
        this.shopId = order.getShopId();
        this.pid = order.getPid();
        this.orderType = order.getOrderType();
        this.freightPrice = order.getFreightPrice();
        this.discountPrice = order.getDiscountPrice();
        this.originPrice = order.getOriginPrice();
        this.state = order.getState();
        this.subState = order.getSubstate();
        this.gmtCreate = order.getGmtCreate().format(Constants.outDateTimeFormatter);;

        this.grouponId = order.getGrouponId();
        this.presaleId = order.getPresaleId();
        this.shipmentSn = order.getShipmentSn();
    }
}
