package cn.edu.xmu.oomall.order.utils;

import cn.edu.xmu.oomall.order.enums.ResponseCode;
import com.github.pagehelper.PageInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 返回对象构造工具类
 * @author Han Li
 * Created at 25/11/2020 8:16 上午
 * Modified by Han Li at 25/11/2020 8:16 上午
 */
public class ResponseUtils {

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
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", code.getCode());
        obj.put("errmsg", code.getMessage());
        return obj;
    }

    /**
     * (200) 创造表示 任何返回码 的 API 标准返回，包含数据
     *
     * @author Han Li
     * Created at 25/11/2020 08:31
     * Created by Han Li at 25/11/2020 08:31
     * @param code 返回码
     * @param data 返回数据
     * @return java.lang.Object
     */
    public static Object make(ResponseCode code, Object data) {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("errno", code.getCode());
        obj.put("errmsg", code.getMessage());
        obj.put("data", data);
        return obj;
    }

    /**
     * (200) 用 Paged APIReturnObject 创造 API 标准返回
     *
     * @author Han Li
     * Created at 25/11/2020 16:06
     * Created by Han Li at 25/11/2020 16:06
     * @param returnObject 原返回 Object
     * @return java.lang.Object 修饰后的返回 Object
     */
    public static Object makePaged(APIReturnObject<PageInfo<?>> returnObject) {
        // 获取 returnObject 中的 PageInfo 对象
        PageInfo<?> pageInfo = returnObject.getData();
        Map<String, Object> embedObj = null;
        // 构造分页返回对象
        if (pageInfo != null) {
            embedObj = new HashMap<>();
            embedObj.put("list", pageInfo.getList());
            embedObj.put("total", pageInfo.getTotal());
            embedObj.put("page", pageInfo.getPageNum());
            embedObj.put("pageSize", pageInfo.getPageSize());
            embedObj.put("pages", pageInfo.getPages());
        }
        // 装入返回体
        if (embedObj != null) {
            return ok(embedObj);
        } else {
            return ok();
        }
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
        switch (returnObject.getCode()) {
            case RESOURCE_NOT_EXIST:
                // 404: 资源不存在，无数据
                return new ResponseEntity<>(
                        make(returnObject.getCode(), returnObject.getErrMsg()),
                        HttpStatus.NOT_FOUND);
            case INTERNAL_SERVER_ERR:
                // 500: 数据库或其他严重错误，无数据
                return new ResponseEntity<>(
                        make(returnObject.getCode(), returnObject.getErrMsg()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case OK:
                // 200: 无错误，可能有数据
                Object data = returnObject.getData();
                if (data != null) {
                    return ok(data);
                } else {
                    return ok();
                }
            default:
                // 200: 其他错误，无数据
                return make(returnObject.getCode(), returnObject.getErrMsg());
        }
    }
}
