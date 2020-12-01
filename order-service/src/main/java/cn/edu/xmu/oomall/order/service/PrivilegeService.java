package cn.edu.xmu.oomall.order.service;

import cn.edu.xmu.oomall.order.connector.PrivilegeConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 权限服务 (功能由【权限模块】提供，这里负责把它们提供的数据进行来到订单模块后的封装)
 *
 * @author Han Li
 * Created at 29/11/2020 11:46 上午
 * Modified by Han Li at 29/11/2020 11:46 上午
 */
@Service
public class PrivilegeService {

    @Autowired
    private PrivilegeConnector privilegeConnector;

    /**
     * 验证 token 并获取管理员用户资料
     *
     * @param token token
     * @return 管理员用户字典
     */
    public Map<String, Object> verifyTokenAndGetAdminInfo(String token) {
        return privilegeConnector.verifyTokenAndGetAdminInfo(token);
    }

}
