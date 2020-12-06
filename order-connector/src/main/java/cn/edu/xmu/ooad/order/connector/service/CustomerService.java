package cn.edu.xmu.ooad.order.connector.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 买家用户服务 (由【其他模块】提供)
 *
 * @author Han Li
 * Created at 25/11/2020 8:54 上午
 * Modified by Han Li at 25/11/2020 8:54 上午
 */
@Service
public class CustomerService {
    /**
     * 验证 token 并获取顾客用户资料
     *
     * @param token token
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> verifyTokenAndGetCustomerInfo(String token) {
        return buildCustomerInfo(1L, "mingqiuxm", "Ming QIU", (short) 1);
    }

    /**
     * 获取顾客用户资料
     *
     * @param userId 用户 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getCustomerInfo(Long userId) {
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
