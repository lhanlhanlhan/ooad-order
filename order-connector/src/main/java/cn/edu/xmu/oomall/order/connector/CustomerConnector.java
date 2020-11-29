package cn.edu.xmu.oomall.order.connector;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 买家用户服务连接器 (方便集成不同组提供的模块，提供高层封装)
 *
 * @author Han Li
 * Created at 25/11/2020 9:33 上午
 * Modified by Han Li at 25/11/2020 9:33 上午
 */
@Component
public class CustomerConnector {

    /**
     * TODO - 从其他模块验证 token 并获取顾客用户资料
     *
     * @param token token
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> verifyTokenAndGetCustomerInfo(String token) {
        // 在这里把其他模块的 Model 转为统一的 Map
        return buildCustomerInfo(1L, "mingqiuxm", "Ming QIU", (short) 1);
    }

    /**
     * TODO - 从其他模块获取顾客用户资料
     *
     * @param userId 用户 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getCustomerInfo(Long userId) {
        // 在这里把其他模块的 Model 转为统一的 Map
        return buildCustomerInfo(1L, "mingqiuxm", "Ming QIU", (short) 1);
    }

    /**
     * 创建客户信息在订单模块的字典
     * @param id
     * @param username
     * @param realName
     * @param state
     * @return
     */
    private Map<String, Object> buildCustomerInfo(Long id, String username, String realName, Short state) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", id);
        userInfo.put("userName", username);
        userInfo.put("realName", realName);
        userInfo.put("state", state);
        return userInfo;
    }
}
