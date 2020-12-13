package cn.edu.xmu.ooad.order.connector.service;

import cn.edu.xmu.ooad.order.require.models.CustomerInfo;
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
     * @return cn.edu.xmu.ooad.order.connector.model.UserInfo
     */
    public CustomerInfo verifyTokenAndGetCustomerInfo(String token) {
        return new CustomerInfo(1L, "mingqiuxm", "Ming QIU", (byte) 1);
    }

    /**
     * 获取顾客用户资料
     *
     * @param userId 用户 Id
     * @return cn.edu.xmu.ooad.order.connector.model.UserInfo
     */
    public CustomerInfo getCustomerInfo(Long userId) {
        return new CustomerInfo(1L, "mingqiuxm", "Ming QIU", (byte) 1);
    }
}
