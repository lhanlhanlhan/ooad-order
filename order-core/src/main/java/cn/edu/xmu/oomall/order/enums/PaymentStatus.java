package cn.edu.xmu.oomall.order.enums;

/**
 * 支付状态枚举
 * @author 苗新宇
 * Created at 27/11/2020 9:26
 * Modified by  苗新宇 at  27/11/2020 9:26
 */
public enum  PaymentStatus {
    PENDING_PAY(0,"待支付"),
    PAID(1,"已支付"),
    FAILED(2,"支付失败"),
    ;
    private final int code;
    private final String desc;

    PaymentStatus(int code, String desc) {
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
