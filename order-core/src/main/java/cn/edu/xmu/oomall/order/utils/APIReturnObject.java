package cn.edu.xmu.oomall.order.utils;

import cn.edu.xmu.oomall.order.enums.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 返回对象
 *
 * @author Han Li
 * Created at 25/11/2020 3:42 下午
 * Modified by Han Li at 25/11/2020 3:42 下午
 */
@Data
@NoArgsConstructor
public class APIReturnObject<T> {

    private ResponseCode code = ResponseCode.OK;
    private String errmsg = null;
    private T data = null;

    /**
     * 带数据的 API 返回对象
     * @param data 数据
     */
    public APIReturnObject(T data) {
        this.data = data;
    }

    /**
     * 带自定义错误码的 API 返回对象
     * @param code 错误码
     */
    public APIReturnObject(ResponseCode code) {
        this.code = code;
    }

    /**
     * 带自定义错误码、返回讯息的 API 返回对象
     * @param code 错误码
     * @param errMsg 返回讯息
     */
    public APIReturnObject(ResponseCode code, String errMsg) {
        this(code);
        this.errmsg = errMsg;
    }

    /**
     * 获取返回讯息 (如有)
     * @return 获取返回讯息，如果自定义返回讯息为空，就获取 Code 绑定的默认返回讯息
     */
    public String getErrMsg() {
        return this.errmsg == null ? this.code.getMessage() : this.errmsg;
    }
}
