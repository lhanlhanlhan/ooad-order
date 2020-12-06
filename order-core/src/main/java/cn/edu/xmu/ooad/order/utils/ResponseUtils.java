package cn.edu.xmu.ooad.order.utils;

import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * API 返回对象构造工具类
 * @author Han Li
 * Created at 25/11/2020 8:16 上午
 * Modified by Han Li at 25/11/2020 8:16 上午
 */
public class ResponseUtils {

    public static final String UNIX_TIMESTAMP_START = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()).toString();

    /**
     * (200) 创造表示 OK 的 API 标准返回，无任何数据
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
     * (200) 创造表示 OK 的 API 标准返回，包含数据
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
     * (200) 创造表示 任何返回码 的 API 标准返回，不包含数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:33
     * Created by Han Li at 25/11/2020 08:33
     * @param code 返回码
     * @return java.lang.Object
     */
    public static Object make(ResponseCode code) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("errno", code.getCode());
        obj.put("errmsg", code.getMessage());
        return obj;
    }


    /**
     * (xxx) 用 APIReturnObject 创造对应 HTTP Status 的 API 标准返回
     *
     * @author Han Li
     * Created at 25/11/2020 16:06
     * Created by Han Li at 25/11/2020 16:06
     * @param returnObject 原返回 Object
     * @return java.lang.Object 修饰后的返回 Object
     */
    public static Object make(APIReturnObject<?> returnObject) {
        Map<String, Object> body = new HashMap<>();
        body.put("errno", returnObject.getCode().getCode());
        body.put("errmsg", returnObject.getErrMsg());
        if (returnObject.getData() != null) {
            body.put("data", returnObject.getData());
        }
        return new ResponseEntity<>(body, returnObject.getStatus());
    }
}
