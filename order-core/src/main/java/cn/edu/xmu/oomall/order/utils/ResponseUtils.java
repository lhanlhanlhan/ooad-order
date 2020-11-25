package cn.edu.xmu.oomall.order.utils;

import cn.edu.xmu.oomall.order.enums.ResponseCode;

import java.util.HashMap;
import java.util.Map;

/**
 * API 返回构造工具类
 * @author Han Li
 * Created at 25/11/2020 8:16 上午
 * Modified by Han Li at 25/11/2020 8:16 上午
 */
public class ResponseUtils {

    /**
     * 创造表示 OK 的 API 标准返回，无任何数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:32
     * Created by Han Li at 25/11/2020 08:32
     * @return java.lang.Object
     */
    public static Object ok() {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", ResponseCode.OK.getCode());
        obj.put("errmsg", ResponseCode.OK.getMessage());
        return obj;
    }

    /**
     * 创造表示 OK 的 API 标准返回，包含数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:31
     * Created by Han Li at 25/11/2020 08:31
     * @param data 要返回的数据
     * @return java.lang.Object
     */
    public static Object ok(Object data) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", ResponseCode.OK.getCode());
        obj.put("errmsg", ResponseCode.OK.getMessage());
        obj.put("data", data);
        return obj;
    }

    /**
     * 创造表示 任何返回码 的 API 标准返回，不包含数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:33
     * Created by Han Li at 25/11/2020 08:33
     * @param code 返回码
     * @return java.lang.Object
     */
    public static Object makeResponse(ResponseCode code) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", code.getCode());
        obj.put("errmsg", code.getMessage());
        return obj;
    }

    /**
     * 创造表示 任何返回码 的 API 标准返回，包含数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:31
     * Created by Han Li at 25/11/2020 08:31
     * @param code 返回码
     * @param data 返回数据
     * @return java.lang.Object
     */
    public static Object makeResponse(ResponseCode code, Object data) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", code.getCode());
        obj.put("errmsg", code.getMessage());
        obj.put("data", data);
        return obj;
    }
}
