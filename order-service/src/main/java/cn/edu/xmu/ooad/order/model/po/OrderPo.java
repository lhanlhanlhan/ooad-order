package cn.edu.xmu.ooad.order.model.po;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 完整订单 Po
 *
 * @author Han Li
 * Created at 26/11/2020 15:06
 * Created by Han Li at 26/11/2020 15:06
 */
@Data
public class OrderPo {
    private Long id;

    private Long customerId;

    private Long shopId;

    private String orderSn;

    private Long pid;

    private String consignee;

    private Long regionId;

    private String address;

    private String mobile;

    private String message;

    private Byte orderType;

    private Long freightPrice;

    private Long couponId;

    private Long couponActivityId;

    private Long discountPrice;

    private Long originPrice;

    private Long presaleId;

    private Long grouponId;

    private Long grouponDiscount;

    private Integer rebateNum;

    private LocalDateTime confirmTime;

    private String shipmentSn;

    private Byte state;

    private Byte substate;

    private Byte beDeleted;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private List<OrderItemPo> orderItemList;
}
