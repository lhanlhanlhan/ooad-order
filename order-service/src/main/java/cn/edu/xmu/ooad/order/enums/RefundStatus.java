package cn.edu.xmu.ooad.order.enums;

/**
 * @author 苗新宇
 * Created at 03/12/2020 0:30
 * Modified by  苗新宇 at  03/12/2020 0:30
 */
public enum RefundStatus {
    NOT_REFUND(0, "未退款"),
    ALREADY_REFUND(1, "已退款"),
    REFUSE_REFUND(2, "拒绝退款"),
    ;
    private final byte code;
    private final String desc;

    RefundStatus(int code, String desc) {
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
