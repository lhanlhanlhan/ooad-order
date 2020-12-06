package cn.edu.xmu.ooad.order.connector.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限系统服务 (由【权限模块】提供)
 *
 * @author Han Li
 * Created at 29/11/2020 11:33 上午
 * Modified by Han Li at 29/11/2020 11:33 上午
 */
@Service
public class PrivilegeService {

    /**
     * TODO - 从管理员模块验证 token 并获取管理员用户资料 (必须是有效用户才能获取到，可以在 PrivilegeService 中做控制)
     *
     * @param token token
     * @return 管理员用户字典
     */
    public Map<String, Object> verifyTokenAndGetAdminInfo(String token) {
        // 在这里把其他模块的 Model 转为统一的 Map
        return buildAdminInfo(1L, "mingqiuxm", 1L);
    }


    /**
     * 创建管理员资料在订单模块中的字典
     * @param id
     * @param username
     * @param shopId
     * @return
     */
    private Map<String, Object> buildAdminInfo(Long id, String username, Long shopId) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", id);
        userInfo.put("username", username);
        userInfo.put("shopId", shopId);
        return userInfo;
    }

}
