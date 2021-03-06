package cn.edu.xmu.ooad.order.centre.utils;

/**
 * 订单模块错误码表
 *
 * @author Han Li
 * Created at 25/11/2020 8:18 上午
 * Modified by Han Li at 25/11/2020 8:18 上午
 */
public enum ResponseCode {
    OK(0, "成功"),

    /**
     * 用户自己的错误
     */
    BAD_REQUEST(400, "您的輸入有誤"),
    NEED_LOGIN(401, "需要登入才可使用"),
    RESOURCE_NOT_EXIST(404, "资源不存在"),
    INVALID_USER(405, "用户不存在或不可使用"),
    REQUEST_NOT_ALLOWED(406, "请求不被服务器支援"),
    ORDER_TYPE_NOT_CORRESPOND(407, "订单种类不支援此操作"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERR(500, "服务器内部错误"),
    INVALID_JWT(501, "JWT不合法"),
    JWT_EXPIRED(502, "JWT过期"),

    /**
     * 订单模块错误
     */
    GOODS_NOT_IN_STOCK(800, "库存不足"),
    ORDER_STATE_NOT_ALLOW(801, "订单状态禁止"),
    FREIGHT_MODEL_NAME_SAME(802, "运费模板名重复"),
    REGION_SAME(803, "运费模板中该地区已经定义"),
    REFUND_MORE(804, "退款金额超过支付金额"),
    FREIGHT_REGION_FORBIDDEN(805, "订购列表中包含禁止寄往该地区之物品"),
    ORDER_MODIFY_REGION_FORBIDDEN(806, "您不能改变寄送目的地区"),
    PAY_MORE(807, "您的付款金额超出了订单总价"),
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
