package cn.edu.xmu.ooad.order.order.enums;

/**
 * 支付状态枚举
 *
 * @author 苗新宇
 * Created at 27/11/2020 9:26
 * Modified by  苗新宇 at  27/11/2020 9:26
 */
public enum PaymentStatus {
    PAID(0, "未支付"),
    PENDING_PAY(1, "已支付"),
    FAILED(2, "支付失败"),
    ;
    private final byte code;
    private final String desc;

    PaymentStatus(int code, String desc) {
        this.code = (byte) code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
