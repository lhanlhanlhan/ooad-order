package cn.edu.xmu.ooad.order.enums;

/**
 * 订单子状态枚举
 *
 * @author Han Li
 * Created at 25/11/2020 12:25 上午
 * Modified by Han Li at 25/11/2020 12:25 上午
 */
public enum OrderChildStatus {

    NEW(11, "新订单", OrderStatus.PENDING_PAY),
    PENDING_REM_BALANCE(12, "待支付尾款", OrderStatus.PENDING_PAY),

    PAID(21, "付款完成", OrderStatus.PENDING_RECEIVE),
    PENDING_GROUP(22, "待成团", OrderStatus.PENDING_RECEIVE),
    GROUP_FAILED(23, "未成团", OrderStatus.PENDING_RECEIVE),
    SHIPPED(24, "已发货", OrderStatus.PENDING_RECEIVE),
    ;

    private final byte code;
    private final String name;
    private final OrderStatus parentStatus;

    OrderChildStatus(int code, String desc, OrderStatus pStatus) {
        this.code = (byte) code;
        this.name = desc;
        this.parentStatus = pStatus;
    }

    public static OrderChildStatus getByCode(int code) {
        byte b = (byte) code;
        OrderChildStatus[] statuses = OrderChildStatus.values();
        if (b > statuses.length) {
            return null;
        }
        return statuses[b];
    }

    public OrderStatus getParentStatus() {
        return parentStatus;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
