package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.UserConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户服务 (功能由【其他模块】提供，这里负责把它们提供的数据进行来到订单模块后的封装)
 *
 * @author Han Li
 * Created at 28/11/2020 11:15 上午
 * Modified by Han Li at 28/11/2020 11:15 上午
 */
@Service
public class UserService {

    @Autowired
    private UserConnector userConnector;

    /**
     * 验证 token 并获取顾客用户资料
     *
     * @param token token
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> verifyTokenAndGetCustomerInfo(String token) {
        return userConnector.verifyTokenAndGetCustomerInfo(token);
    }

    /**
     * 获取顾客用户资料
     *
     * @param userId 用户 Id
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public Map<String, Object> getCustomerInfo(Long userId) {
        return userConnector.getCustomerInfo(userId);
    }
}
