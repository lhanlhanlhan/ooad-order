package cn.edu.xmu.ooad.order.order.model.vo;

import cn.edu.xmu.ooad.order.order.enums.OrderChildStatus;
import cn.edu.xmu.ooad.order.order.enums.OrderStatus;
import cn.edu.xmu.ooad.order.order.model.bo.order.Order;
import cn.edu.xmu.ooad.order.require.models.CustomerInfo;
import cn.edu.xmu.ooad.order.require.models.ShopInfo;
import cn.edu.xmu.ooad.order.centre.utils.ResponseUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单完整信息 (后端返回)
 *
 * @author Han Li
 * Created at 26/11/2020 11:16 上午
 * Modified by Han Li at 26/11/2020 11:16 上午
 */
@Data
public class OrderVo {

    private final Long id;
    private CustomerInfo customer;
    private ShopInfo shop;
    private Long pid;
    private Byte orderType;
    private Byte state;
    private Byte subState;
    private String gmtCreate;
    private String gmtModified;
    private String confirmTime;
    private Long originPrice;
    private Long discountPrice;
    private Long freightPrice;
    private String message;
    private Long regionId;
    private String address;
    private String mobile;
    private String consignee;
    private Long couponId;
    private Long grouponId;
    private Long presaleId;
    private String shipmentSn;
    private List<OrderItemVo> orderItems;

    /**
     * 用一个 Bo 对象初始化本对象
     *
     * @param order Bo 对象
     */
    public OrderVo(Order order) {
        // Customer、Shop 的 Vo 先不获取，在 Service 层才获取
        this.customer = null;
        this.shop = null;

        // 构造下述对象
        this.id = order.getId();
        this.pid = order.getPid();
        this.orderType = order.getOrderType().getCode();

        OrderStatus state = order.getState();
        OrderChildStatus subState = order.getSubstate();
        this.state = state == null ? null : state.getCode();
        this.subState = subState == null ? null : subState.getCode();

        this.originPrice = order.getOriginPrice();
        this.discountPrice = order.getDiscountPrice();
        this.freightPrice = order.getFreightPrice();
        this.message = order.getMessage();
        this.regionId = order.getRegionId();
        this.address = order.getAddress();
        this.mobile = order.getMobile();
        this.consignee = order.getConsignee();
        this.couponId = order.getCouponId();
        this.grouponId = order.getGrouponId();
        this.presaleId = order.getPresaleId();
        this.shipmentSn = order.getShipmentSn();
        this.orderItems = // 如果是父訂單，這邊就會是 null
                order.getOrderItemList() == null ?
                        null :
                        order.getOrderItemList()
                                .stream()
                                .map(OrderItemVo::new)
                                .collect(Collectors.toList());

        LocalDateTime gmtCreate = order.getGmtCreated();
        LocalDateTime gmtModified = order.getGmtModified();
        LocalDateTime confirmTime = order.getConfirmTime();
        this.gmtCreate = gmtCreate == null ? ResponseUtils.UNIX_TIMESTAMP_START : gmtCreate.toString();
        this.gmtModified = gmtModified == null ? this.gmtCreate : gmtModified.toString();
        this.confirmTime = confirmTime == null ? null : confirmTime.toString();
    }
}
