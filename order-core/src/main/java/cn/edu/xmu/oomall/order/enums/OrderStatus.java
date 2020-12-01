package cn.edu.xmu.oomall.order.enums;

/**
 * 订单状态枚举
 * @author Han Li
 * Created at 25/11/2020 12:25 上午
 * Modified by Han Li at 25/11/2020 12:25 上午
 */
public enum OrderStatus {
    NEW(6, "创建订单"),
    CANCELLED(0, "订单取消"),
    PENDING_DEPOSIT(1, "待支付定金"),
    PENDING_PAY(2, "待支付"),
    PENDING_GROUP(3, "待参团"),
    DEPOSIT_PAID(4, "已支付定金"),
    PENDING_REM_BALANCE(5, "待支付尾款"),
    PRE_SALE_TERMINATED(7, "预售中止"),
    GROUP_FORMED(8, "已参团"),
    GROUP_FAILED(9, "团购未到达门槛"),
    GROUPED(10, "已成团"),
    PAID(11, "已支付"),
    REM_BALANCE_PAID(12, "已支付尾款"),
    REFUNDED(13, "已退款"),
    TERMINATED(14, "订单中止"),
    AFTER_SALE_PENDING_SHIPMENT(15, "售后单待发货"),
    SHIPPED(16, "发货中"),
    REACHED(17, "到货"),
    SIGNED(18, "已签收"),
    ;

    private final byte code;
    private final String name;

    OrderStatus(int code, String desc) {
        this.code = (byte) code;
        this.name = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static OrderStatus getByCode(int code) {
        byte b = (byte) code;
        OrderStatus[] statuses = OrderStatus.values();
        if (b > statuses.length) {
            return null;
        }
        return statuses[b];
    }
}
