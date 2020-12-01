package cn.edu.xmu.oomall.order.model.vo;

import cn.edu.xmu.oomall.order.model.bo.Order;
import cn.edu.xmu.oomall.order.utils.ResponseUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Han Li
 * Created at 26/11/2020 11:16 上午
 * Modified by Han Li at 26/11/2020 11:16 上午
 */
@Data
public class OrderVo {

    private final Long id;
    private Map<String, Object> customer;
    private Map<String, Object> shop;
    private Long pid;
    private Byte orderType;
    private Byte state;
    private Byte subState;
    private String gmtCreate;
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
    private List<OrderItemVo> orderItems;

    /**
     * 用一个 Bo 对象初始化本对象
     * @param order Bo 对象
     */
    public OrderVo(Order order) {
        // Customer、Shop 的 Vo 先不获取，在 Service 层才获取
        this.customer = null;
        this.shop = null;

        // 构造下述对象
        this.id = order.getId();
        this.pid = order.getPid();
        this.orderType = order.getOrderType();
        this.state = order.getState();
        this.subState = order.getSubstate();
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
        this.orderItems = order.getOrderItemList().stream().map(OrderItemVo::new).collect(Collectors.toList());

        // TODO - 这个 API 是数字，但是其他 API 大都是 String，没见过数字，咋整
        LocalDateTime gmtCreate = order.getGmtCreated();
        this.gmtCreate = gmtCreate == null ? ResponseUtils.UNIX_TIMESTAMP_START : gmtCreate.toString();
    }

    private Map<String, Object> buildCustomerVo(Map<String, Object> data) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", data.get("id"));
        userInfo.put("userName", data.get("userName"));
        userInfo.put("realName", data.get("realName"));
        return userInfo;
    }

    private Map<String, Object> buildShopVo(Map<String, Object> data) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", data.get("id"));
        userInfo.put("name", data.get("name"));
        userInfo.put("gmtCreate", data.get("gmtCreate"));
        userInfo.put("gmtModified", data.get("gmtModified"));
        return userInfo;
    }
}
