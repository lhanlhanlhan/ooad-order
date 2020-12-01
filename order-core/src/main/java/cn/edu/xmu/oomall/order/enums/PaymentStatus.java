package cn.edu.xmu.oomall.order.enums;

/**
 * 支付状态枚举
 * @author 苗新宇
 * Created at 27/11/2020 9:26
 * Modified by  苗新宇 at  27/11/2020 9:26
 */
public enum  PaymentStatus {
    PAID((byte)0,"已支付"),
    PENDING_PAY((byte)1,"未支付"),
    FAILED((byte)2,"支付失败"),
    ;
    private final Byte code;
    private final String desc;

    PaymentStatus(Byte code, String desc) {
        this.code=code;
        this.desc=desc;
    }
    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
