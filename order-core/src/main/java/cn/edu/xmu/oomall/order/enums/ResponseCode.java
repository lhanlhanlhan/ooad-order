package cn.edu.xmu.oomall.order.enums;

/**
 * 订单模块错误码表
 * @author Han Li
 * Created at 25/11/2020 8:18 上午
 * Modified by Han Li at 25/11/2020 8:18 上午
 */
public enum ResponseCode {
    OK(0, "成功"),

    /**
     * 用户自己的错误
     */
    NEED_LOGIN(401, "需要登入才可使用"),
    RESOURCE_NOT_EXIST(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERR(500,"服务器内部错误"),
    INVALID_JWT(501,"JWT不合法"),
    JWT_EXPIRED(502,"JWT过期"),

    /**
     * 订单模块错误
     */
    ORDER_DISTORTED(800, "订单被篡改"),
    ORDER_STATE_NOT_ALLOW(801, "订单状态禁止"),
    FREIGHT_MODEL_NAME_SAME(802, "运费模板名重复"),
    REGION_SAME(803, "运费模板中该地区已经定义"),
    REFUND_MORE(804, "退款金额超过支付金额"),
    ;

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
