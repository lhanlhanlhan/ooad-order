package cn.edu.xmu.ooad.order.enums;

/**
 * 订单状态枚举
 *
 * @author Han Li
 * Created at 25/11/2020 12:25 上午
 * Modified by Han Li at 25/11/2020 12:25 上午
 */
public enum OrderStatus {

    PENDING_PAY(1, "待支付"),
    PENDING_RECEIVE(2, "待收货"),
    DONE(3, "已完成"),
    CANCELLED(4, "已取消"),
    ;

    private final byte code;
    private final String name;

    OrderStatus(int code, String desc) {
        this.code = (byte) code;
        this.name = desc;
    }

    public static OrderStatus getByCode(int code) {
        byte b = (byte) code;
        OrderStatus[] statuses = OrderStatus.values();
        if (b > statuses.length) {
            return null;
        }
        return statuses[b];
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
