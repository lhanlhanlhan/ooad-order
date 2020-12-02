package cn.edu.xmu.oomall.order.enums;

/**
 * @author Han Li
 * Created at 2/12/2020 2:46 下午
 * Modified by Han Li at 2/12/2020 2:46 下午
 */
public enum OrderType {

    NORMAL(0, "普通订单"),
    GROUPON(1, "团购订单"),
    PRE_SALE(2, "预售订单");

    private final byte code;
    private final String name;

    OrderType(int code, String desc) {
        this.code = (byte) code;
        this.name = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static OrderType getTypeFromCode(byte code) {
        OrderType[] values = OrderType.values();
        return code > values.length ? null : values[code];
    }
}
