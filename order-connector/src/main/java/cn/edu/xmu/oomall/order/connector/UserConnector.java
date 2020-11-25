package cn.edu.xmu.oomall.order.connector;

import cn.edu.xmu.oomall.order.connector.model.UserInfo;
import org.springframework.stereotype.Component;

/**
 * 买家用户服务连接器 (方便集成不同组提供的模块，提供高层封装)
 *
 * @author Han Li
 * Created at 25/11/2020 9:33 上午
 * Modified by Han Li at 25/11/2020 9:33 上午
 */
@Component
public class UserConnector {

    /**
     * TODO - 从其他模块验证 token 并获取用户资料
     *
     * @author Han Li
     * Created at 25/11/2020 09:41
     * Created by Han Li at 25/11/2020 09:41
     * @param token token
     * @return cn.edu.xmu.oomall.order.connector.model.UserInfo
     */
    public UserInfo verifyTokenAndGetUserInfo(String token) {
        return new UserInfo(1L, "Ming QIU", (short) 1);
    }

}
