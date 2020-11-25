package cn.edu.xmu.oomall.order.enums;

/**
 * 订单状态枚举
 * @author Han Li
 * Created at 25/11/2020 12:25 上午
 * Modified by Han Li at 25/11/2020 12:25 上午
 */
public enum OrderStatus {
    NEW(0, "新订单"),
    PENDING_DEPOSIT(1, "待支付定金"),
    PENDING_PAY(2, "待支付"),
    PENDING_GROUP(3, "待参团"),
    DEPOSIT_PAID(4, "已支付定金"),
    PENDING_REM_BALANCE(5, "待支付尾款"),
    REM_BALANCE_PAID(6, "已支付尾款"),
    PAID(7, "已支付"),
    GROUP_FORMED(8, "已参团"),
    GROUP_FAILED(9, "团购未到达门槛"),
    GROUPED(10, "已成团"),
    SHIPPED(11, "发货中"),
    REACHED(12, "到货"),
    SIGNED(13, "已签收"),
    REFUNDED(14, "已签收"),
    TERMINATED(15, "订单中止"),
    PRE_SALE_TERMINATED(16, "订单中止"),
    CANCELLED(17, "订单取消"),
    AFTER_SALE_PENDING_SHIPMENT(18, "售后单待发货"),
    ;

    private final int code;
    private final String name;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.name = desc;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
