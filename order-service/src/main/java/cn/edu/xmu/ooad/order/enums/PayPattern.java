package cn.edu.xmu.ooad.order.enums;

import reactor.util.annotation.Nullable;

/**
 * @author Han Li
 * Created at 5/12/2020 3:06 下午
 * Modified by Han Li at 5/12/2020 3:06 下午
 */
public enum PayPattern {
    REBATE("001", "返点支付"),
    MOCK("002", "模拟支付渠道"),
    ;
    private final String code;
    private final String desc;

    PayPattern(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Nullable
    public static PayPattern ofCode(String code) {
        for (PayPattern payPattern : PayPattern.values()) {
            if (payPattern.getCode().equals(code)) {
                return payPattern;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
